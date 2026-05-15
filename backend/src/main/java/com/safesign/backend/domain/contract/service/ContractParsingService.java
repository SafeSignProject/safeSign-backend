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
        int lastMatchEndIndex = 0; // [수정] 마지막 제N조가 끝난 인덱스를 저장할 변수

        Pattern clausePattern = Pattern.compile(
                "([제체체]\\s*\\d+\\s*조)\\s*(?:[(\\[【<]([^)\\]】>]+)[)\\]】>])?\\s*(.*?)(?=[제체체]\\s*\\d+\\s*조|특\\s*약\\s*사\\s*항|특\\s*약|\\[\\s*특\\s*약\\s*\\]|$)",
                Pattern.DOTALL
        );
        Matcher matcher = clausePattern.matcher(text);

        while (matcher.find()) {
            String clauseNo = matcher.group(1).replaceAll("\\s+", "").replace("체", "제");
            String rawTitle = matcher.group(2);
            String rawContent = matcher.group(3);

            // 공백 정제
            String content = rawContent != null ? rawContent.replaceAll("\\s+", " ").trim() : "";
            String title;

            if (rawTitle != null && !rawTitle.isBlank()) {
                // 괄호 제목이 있는 경우 (예: 제1조 [목적])
                title = clauseNo + " [" + rawTitle.replaceAll("\\s+", " ").trim() + "]";
            } else {
                title = clauseNo;
            }

            if (content.length() >= 2) {
                clauses.add(new ClauseResponse(title, content, orderNo++));
            }

            // [수정] 매칭이 성공할 때마다 끝나는 지점의 인덱스를 계속 업데이트
            lastMatchEndIndex = matcher.end();
        }

        // [수정] 마지막 조항이 끝난 지점부터 계약서 끝까지의 잔여 텍스트를 추출
        String potentialSpecialText = text.substring(lastMatchEndIndex);

        // [수정] 추출한 잔여 텍스트를 특약사항 분석 메서드로 전달
        List<String> specialTerms = extractSpecialTerms(potentialSpecialText);
        for (String special : specialTerms) {
            clauses.add(new ClauseResponse("특약사항", special, orderNo++));
        }

        return clauses;
    }

    // [수정] 특약사항 세부 문장 추출 (마지막 조항 이후의 잔여 텍스트를 받아 처리)
    private List<String> extractSpecialTerms(String remainingText) {
        List<String> result = new ArrayList<>();
        String[] lines = remainingText.split("\n");

        // 하단 서명부나 날짜 영역을 만나면 수집을 중단하기 위한 패턴
        Pattern endPattern = Pattern.compile("^\\s*(본\\s*계약에|위와\\s*같이|계약을\\s*체결|\\d{4}년\\s*\\d{1,2}월|임\\s*대\\s*인\\s*$|매\\s*도\\s*인\\s*$|임\\s*차\\s*인\\s*$)");

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;

            // 서명부나 날짜를 만나면 특약사항 수집 즉시 종료
            if (endPattern.matcher(trimmedLine).find()) {
                break;
            }

            // 만약 '[특약사항]' 같은 타이틀 줄이 텍스트에 포함되어 있다면, 데이터로 저장하지 않고 패스
            if (trimmedLine.matches(".*(특\\s*약\\s*사\\s*항|특\\s*약\\s*조\\s*건|특\\s*특\\s*약).*")) {
                continue;
            }

            // 1., 2., *, ①, 2-1. 등의 불릿 기호 처리 정규식
            Matcher bulletMatcher = Pattern.compile("^(?:\\d+\\.|-|\\*|①|②|③|④|⑤|\\d+-\\d+\\.)\\s*(.+)").matcher(trimmedLine);

            if (bulletMatcher.find()) {
                String content = bulletMatcher.group(1).trim();
                if (content.length() >= 2) {
                    result.add(content);
                }
            } else {
                // 불릿 번호가 없더라도 의미 있는 문장이면 특약사항으로 인정 (노이즈 방지를 위해 최소 4자 이상)
                if (trimmedLine.length() >= 4) {
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