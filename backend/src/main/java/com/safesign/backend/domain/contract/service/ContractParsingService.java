package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.*;
import com.safesign.backend.domain.contract.entity.*;
import com.safesign.backend.domain.contract.repository.*;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContractParsingService {

    private final ContractRepository contractRepository;
    private final ContractOcrResultRepository ocrRepository;
    private final ContractClauseRepository clauseRepository;
    private final ContractHeaderInfoRepository headerRepository;

    public ParsingResponse parse(Long userId, Long contractId) {

        // 1. 계약 및 OCR 조회
        Contract contract = contractRepository
                .findByContractIdAndUser_UserId(contractId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        ContractOcrResult ocr = ocrRepository
                .findLatestOcrResult(contract)
                .orElseThrow(() -> new CustomException(ErrorCode.OCR_RESULT_NOT_FOUND));

        // 2. OCR 텍스트 전처리 및 줄 단위 분리
        String cleanText = preprocess(ocr.getFullText());
        List<String> lines = Arrays.stream(cleanText.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        // 3. 기존 데이터 초기화 (조항 & 표제부)
        clauseRepository.deleteByContract(contract);
        headerRepository.deleteByContract(contract);

        // 4. 조항 단위 분리 및 엔티티 저장
        List<ClauseResponse> clauses = extractClauses(cleanText);

        for (ClauseResponse clause : clauses) {
            String clauseNo = normalizeClauseNo(clause.getTitle());

            ContractClause entity = ContractClause.builder()
                    .contract(contract)
                    .clauseNo(clauseNo)
                    .clauseTitle(clause.getTitle())
                    .clauseText(clause.getContent())
                    .clauseType(clauseNo.startsWith("제") ? "STANDARD" : "SPECIAL")
                    .orderNo(clause.getOrderNo())
                    .build();

            clauseRepository.save(entity);
        }

        // 5. 표제부(Header) 추출
        HeaderResponse header = extractHeaderInfo(lines);

        // 6. 표제부(Header)
        ContractHeaderInfo headerEntity = ContractHeaderInfo.builder()
                .contract(contract)
                .propertyAddress(header.getAddress())
                .depositAmount(parseMoney(header.getDeposit()))
                .contractPayment(parseMoney(header.getContractAmount()))
                .monthlyRent(parseMoney(header.getMonthlyRent()))
                // 아래 항목들은 현재 정규식으로 완벽히 뽑기 어려워 null로 들어갈 수 있음
                .propertyArea(null)
                .intermediatePayment(null)
                .balancePayment(null)
                .brokerFee(null)
                .landlordName(null)
                .build();

        headerRepository.save(headerEntity);

        // 7. 결과 반환
        return new ParsingResponse(
                contract.getContractId(),
                clauses.size(),
                "파싱 및 저장 완료",
                lines,
                clauses,
                header
        );
    }

    // OCR 전처리 (노이즈 제거)
    private String preprocess(String text) {
        return text
                .replaceAll(":(un)?selected:", "")
                .replaceAll("\\[[V√]]", "")
                .replaceAll("\\r", "")
                .replaceAll("\\n+", "\n")
                .trim();
    }

    // 표제부(Header) 추출 (주소, 보증금, 월세, 계약금)
    private HeaderResponse extractHeaderInfo(List<String> lines) {
        String address = null, deposit = null, monthlyRent = null, contractAmount = null;

        for (String line : lines) {
            if (line.contains("소재지") || line.contains("부동산의 표시")) {
                address = line.replaceAll("소재지|1\\.\\s*부동산의 표시", "").trim();
            }
            if (line.contains("보증금")) {
                deposit = extractMoneyStr(line);
            }
            if (line.contains("차 임") || line.contains("차임")) {
                monthlyRent = extractMoneyStr(line);
            }
            if (line.contains("계약금")) {
                contractAmount = extractMoneyStr(line);
            }
        }
        return new HeaderResponse(address, null, null, null, null, null, null, null, deposit, contractAmount, null, null, monthlyRent);
    }

    // 문자열 금액 추출
    private String extractMoneyStr(String line) {
        Matcher m = Pattern.compile("[₩\\\\]?\\s*([0-9,]+)").matcher(line);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    // 숫자 금액 파싱
    private Long parseMoney(String value) {
        if (value == null) return null;
        String onlyNumber = value.replaceAll("[^0-9]", "");
        if (onlyNumber.isBlank()) return null;
        return Long.parseLong(onlyNumber);
    }

    // 조항 추출 (제N조 및 특약사항)

    private List<ClauseResponse> extractClauses(String text) {
        List<ClauseResponse> clauses = new ArrayList<>();
        int orderNo = 1;

        Pattern clausePattern = Pattern.compile(
                "([제체]\\s*\\d+\\s*조)[).]?\\s*[(\\[【<]([^)\\]】>]+)[)\\]】>]\\s*(.*?)(?=[제체]\\s*\\d+\\s*조|특\\s*약|본\\s*계약|$)",
                Pattern.DOTALL
        );
        Matcher matcher = clausePattern.matcher(text);

        while (matcher.find()) {
            String clauseNo = matcher.group(1).replaceAll("\\s+", "").replace("체", "제");
            String title = matcher.group(2).replaceAll("\\s+", " ").trim();
            String content = matcher.group(3).replaceAll("\\s+", " ").trim();

            if (content.length() >= 5) {
                clauses.add(new ClauseResponse(clauseNo + " [" + title + "]", content, orderNo++));
            }
        }

        List<String> specialTerms = extractSpecialTerms(text);
        for (String special : specialTerms) {
            clauses.add(new ClauseResponse("특약사항", special, orderNo++));
        }

        return clauses;
    }

    // 특약사항 세부 문장 추출
    private List<String> extractSpecialTerms(String text) {
        List<String> result = new ArrayList<>();
        String[] lines = text.split("\n");
        boolean isSpecialSection = false;

        Pattern startPattern = Pattern.compile("특\\s*약\\s*사\\s*항|특\\s*약\\s*조\\s*건|특\\s*수\\s*조\\s*건|특\\s*약|\\[\\s*특\\s*약\\s*]");
        Pattern endPattern = Pattern.compile("^\\s*(본\\s*계약에|위와\\s*같이|계약을\\s*체결|\\d{4}년\\s*\\d{1,2}월|임\\s*대\\s*인\\s*$|매\\s*도\\s*인\\s*$|임\\s*차\\s*인\\s*$)");

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;

            if (!isSpecialSection && startPattern.matcher(trimmedLine).find()) {
                isSpecialSection = true;
                continue;
            }

            if (!isSpecialSection) continue;

            if (endPattern.matcher(trimmedLine).find()) {
                break;
            }

            Matcher bulletMatcher = Pattern.compile("^(?:\\d+\\.|-|\\*|①|②|③|④|⑤)\\s*(.+)").matcher(trimmedLine);

            if (bulletMatcher.find()) {
                result.add(bulletMatcher.group(1).trim());
            } else {
                if (trimmedLine.length() >= 5) {
                    result.add(trimmedLine);
                }
            }
        }

        return result;
    }

    // 조항번호 정규화
    private String normalizeClauseNo(String title) {
        Matcher matcher = Pattern.compile("제\\d+조|\\d+\\.").matcher(title);
        if (matcher.find()) {
            return matcher.group();
        }
        return title.length() > 20 ? title.substring(0, 20) : title;
    }
}