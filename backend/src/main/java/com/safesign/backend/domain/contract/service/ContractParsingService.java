package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.*;
import com.safesign.backend.domain.contract.entity.*;
import com.safesign.backend.domain.contract.repository.*;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractParsingService {

    private final ContractRepository contractRepository;
    private final ContractOcrResultRepository ocrRepository;

    private final ContractClauseRepository clauseRepository;
    private final ContractHeaderInfoRepository headerRepository;

    public ParsingResponse parse(Long userId, Long contractId) {

        // 계약 조회
        Contract contract = contractRepository
                .findByContractIdAndUser_UserId(contractId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        // OCR 조회
        ContractOcrResult ocr = ocrRepository
                .findLatestOcrResult(contract)
                .orElseThrow(() -> new CustomException(ErrorCode.OCR_RESULT_NOT_FOUND));

        // OCR 전처리
        String text = preprocess(ocr.getFullText());

        String[] lines = text.split("\n");

        // 기존 데이터 삭제
        clauseRepository.deleteByContract(contract);
        headerRepository.deleteByContract(contract);

        // HEADER
        HeaderResponse header = extractHeaderInfo(lines);

        // 당사자
        List<PartyResponse> parties = extractParties(lines);

        // HEADER 저장
        ContractHeaderInfo headerEntity = ContractHeaderInfo.builder()
                .contract(contract)

                .landlordName(
                        parties.isEmpty()
                                ? null
                                : parties.get(0).getLandlordName()
                )

                .propertyAddress(header.getAddress())

                .propertyArea(parseArea(header.getLeasedArea()))

                .depositAmount(parseMoney(header.getDeposit()))

                .monthlyRent(parseMoney(header.getMonthlyRent()))

                .contractPayment(parseMoney(header.getContractAmount()))

                .intermediatePayment(parseMoney(header.getMiddlePayment()))

                .balancePayment(parseMoney(header.getBalance()))

                .brokerFee(extractBrokerFee(text))

                .build();

        headerRepository.save(headerEntity);

        // 조항 파싱
        List<ClauseResponse> clauses = extractClauses(text);

        // 저장
        for (ClauseResponse clause : clauses) {

            String clauseNo = normalizeClauseNo(clause.getTitle());

            ContractClause entity = ContractClause.builder()
                    .contract(contract)

                    .clauseNo(clauseNo)

                    .clauseTitle(clause.getTitle())

                    .clauseText(clause.getContent())

                    .clauseType(
                            clauseNo.startsWith("제")
                                    ? "STANDARD"
                                    : "SPECIAL"
                    )

                    .orderNo(clause.getOrderNo())

                    .build();

            clauseRepository.save(entity);
        }

        // 중개사
        List<AgentResponse> agents = extractAgents(lines);

        // 계약일
        String contractDate = extractContractDate(text);

        return new ParsingResponse(
                contract.getContractId(),
                clauses.size(),
                "파싱 완료",
                header,
                clauses,
                parties,
                agents,
                contractDate
        );
    }

    // =====================================================
    // OCR 전처리
    // =====================================================

    private String preprocess(String text) {

        return text

                .replaceAll(":selected:", "")
                .replaceAll(":unselected:", "")

                .replaceAll("\\[V]", "")
                .replaceAll("\\[√]", "")

                .replaceAll("건\\s*물", "건물")
                .replaceAll("소\\s*재\\s*지", "소재지")

                .replaceAll("계\\s*약\\s*금", "계약금")
                .replaceAll("중\\s*도\\s*금", "중도금")
                .replaceAll("잔\\s*금", "잔금")
                .replaceAll("차\\s*임", "차임")

                .replaceAll("\\r", "")

                .replaceAll("\\n+", "\n")

                .trim();
    }

    // =====================================================
    // HEADER 추출
    // =====================================================

    private HeaderResponse extractHeaderInfo(String[] lines) {

        StringBuilder builder = new StringBuilder();

        for (String line : lines) {

            builder.append(line).append(" ");
        }

        String fullText = builder.toString();

        String address = null;

        String deposit = null;

        String monthlyRent = null;

        String contractAmount = null;

        String middlePayment = null;

        String balance = null;

        String leasedArea = null;

        // 주소
        Matcher addressMatcher = Pattern.compile(
                "소재지\\s*([가-힣0-9\\-\\s]+(?:번지|로|길).*?\\d+)"
        ).matcher(fullText);

        if (addressMatcher.find()) {

            address = addressMatcher.group(1)
                    .replaceAll("\\s+", " ")
                    .trim();
        }

        // 면적
        Matcher areaMatcher = Pattern.compile(
                "약\\s*(\\d+)\\s*m2"
        ).matcher(fullText);

        if (areaMatcher.find()) {

            leasedArea = "약 " + areaMatcher.group(1) + " m2";
        }

        // 보증금
        Matcher depositMatcher = Pattern.compile(
                "보증금.*?(₩?[\\d,]+)"
        ).matcher(fullText);

        if (depositMatcher.find()) {

            deposit = depositMatcher.group(1);
        }

        // 월세
        Matcher rentMatcher = Pattern.compile(
                "차임.*?(₩?[\\d,]+)"
        ).matcher(fullText);

        if (rentMatcher.find()) {

            monthlyRent = rentMatcher.group(1);
        }

        // 계약금
        Matcher contractMatcher = Pattern.compile(
                "계약금.*?(₩?[\\d,]+)"
        ).matcher(fullText);

        if (contractMatcher.find()) {

            contractAmount = contractMatcher.group(1);
        }

        // 중도금
        Matcher middleMatcher = Pattern.compile(
                "중도금.*?(₩?[\\d,]+)"
        ).matcher(fullText);

        if (middleMatcher.find()) {

            middlePayment = middleMatcher.group(1);
        }

        // 잔금
        Matcher balanceMatcher = Pattern.compile(
                "잔금.*?(₩?[\\d,]+)"
        ).matcher(fullText);

        if (balanceMatcher.find()) {

            balance = balanceMatcher.group(1);
        }

        return new HeaderResponse(
                address,
                null,
                null,
                null,
                null,
                null,
                null,
                leasedArea,
                deposit,
                contractAmount,
                middlePayment,
                balance,
                monthlyRent
        );
    }

    // =====================================================
    // 조항 추출
    // =====================================================

    private List<ClauseResponse> extractClauses(String text) {

        List<ClauseResponse> clauses = new ArrayList<>();

        Pattern clausePattern = Pattern.compile(
                "(제\\d+조)\\)?\\s*\\[([^\\]]+)]\\s*(.*?)(?=제\\d+조\\)|특약사항|$)",
                Pattern.DOTALL
        );

        Matcher matcher = clausePattern.matcher(text);

        int orderNo = 1;

        while (matcher.find()) {

            String clauseNo = matcher.group(1);

            String title = matcher.group(2)
                    .replaceAll("\\s+", " ")
                    .trim();

            String content = matcher.group(3)
                    .replaceAll("\\s+", " ")
                    .trim();

            if (content.length() < 5) {
                continue;
            }

            clauses.add(
                    new ClauseResponse(
                            clauseNo + " [" + title + "]",
                            content,
                            orderNo++
                    )
            );
        }

        // 특약사항
        List<String> specialTerms = extractSpecialTerms(text);

        for (String special : specialTerms) {

            clauses.add(
                    new ClauseResponse(
                            "특약사항",
                            special,
                            orderNo++
                    )
            );
        }

        return clauses;
    }

    // =====================================================
    // 특약사항
    // =====================================================

    private List<String> extractSpecialTerms(String text) {

        List<String> result = new ArrayList<>();

        String[] lines = text.split("\n");

        boolean specialStart = false;

        for (String line : lines) {

            line = line.trim();

            if (line.contains("특약사항")) {

                specialStart = true;

                continue;
            }

            if (!specialStart) {
                continue;
            }

            if (line.contains("본 계약에 대하여")) {
                break;
            }

            Matcher matcher = Pattern.compile(
                    "^\\d+\\.\\s*(.+)"
            ).matcher(line);

            if (matcher.find()) {

                result.add(
                        matcher.group(1).trim()
                );
            }
        }

        return result;
    }

    // =====================================================
    // 당사자 추출
    // =====================================================

    private List<PartyResponse> extractParties(String[] lines) {

        StringBuilder builder = new StringBuilder();

        for (String line : lines) {

            builder.append(line).append(" ");
        }

        String fullText = builder.toString();

        String landlordName = null;
        String landlordPhone = null;
        String landlordAddress = null;

        String tenantName = null;
        String tenantPhone = null;
        String tenantAddress = null;

        // 임대인 이름
        Matcher landlordMatcher = Pattern.compile(
                "임대인.*?성\\s*명\\s*([가-힣]+)"
        ).matcher(fullText);

        if (landlordMatcher.find()) {

            landlordName = landlordMatcher.group(1);
        }

        // 임대인 전화번호
        Matcher landlordPhoneMatcher = Pattern.compile(
                "임대인.*?(01[0-9]-\\d{3,4}-\\d{4})"
        ).matcher(fullText);

        if (landlordPhoneMatcher.find()) {

            landlordPhone = landlordPhoneMatcher.group(1);
        }

        // 임차인 이름
        Matcher tenantMatcher = Pattern.compile(
                "임차인.*?성\\s*명\\s*([가-힣]+)"
        ).matcher(fullText);

        if (tenantMatcher.find()) {

            tenantName = tenantMatcher.group(1);
        }

        // 임차인 전화번호
        Matcher tenantPhoneMatcher = Pattern.compile(
                "임차인.*?(01[0-9]-\\d{3,4}-\\d{4})"
        ).matcher(fullText);

        if (tenantPhoneMatcher.find()) {

            tenantPhone = tenantPhoneMatcher.group(1);
        }

        return List.of(
                new PartyResponse(
                        landlordName,
                        landlordPhone,
                        landlordAddress,
                        null,
                        tenantName,
                        tenantPhone,
                        tenantAddress,
                        null
                )
        );
    }

    // =====================================================
    // 중개사 추출
    // =====================================================

    private List<AgentResponse> extractAgents(String[] lines) {

        List<AgentResponse> result = new ArrayList<>();

        StringBuilder builder = new StringBuilder();

        for (String line : lines) {

            builder.append(line).append(" ");
        }

        String text = builder.toString();

        Pattern pattern = Pattern.compile(
                "사무소명칭\\s*([가-힣A-Za-z0-9]+).*?" +
                        "사무소소재지\\s*([가-힣0-9\\-\\s()]+).*?" +
                        "(\\d{5}-\\d{4}-\\d{5}|가-\\d+-\\d+-\\d+)",
                Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {

            result.add(
                    new AgentResponse(
                            matcher.group(1).trim(),
                            matcher.group(2).trim(),
                            null,
                            matcher.group(3).trim()
                    )
            );
        }

        return result;
    }

    // =====================================================
    // 계약일 추출
    // =====================================================

    private String extractContractDate(String text) {

        Matcher matcher = Pattern.compile(
                "\\d{4}년\\s*\\d{2}월\\s*\\d{2}일"
        ).matcher(text);

        return matcher.find()
                ? matcher.group()
                : null;
    }

    // =====================================================
    // 조항번호 정리
    // =====================================================

    private String normalizeClauseNo(String clauseNo) {

        Matcher matcher = Pattern.compile(
                "제\\d+조|\\d+\\."
        ).matcher(clauseNo);

        if (matcher.find()) {

            return matcher.group();
        }

        return clauseNo.length() > 20
                ? clauseNo.substring(0, 20)
                : clauseNo;
    }

    // =====================================================
    // 금액 파싱
    // =====================================================

    private Long parseMoney(String value) {

        if (value == null) {
            return null;
        }

        String onlyNumber = value.replaceAll("[^0-9]", "");

        if (onlyNumber.isBlank()) {
            return null;
        }

        return Long.parseLong(onlyNumber);
    }

    // =====================================================
    // 면적 파싱
    // =====================================================

    private BigDecimal parseArea(String value) {

        if (value == null) {
            return null;
        }

        String onlyNumber = value.replaceAll("[^0-9.]", "");

        if (onlyNumber.isBlank()) {
            return null;
        }

        return new BigDecimal(onlyNumber);
    }

    // =====================================================
    // 중개보수
    // =====================================================

    private Long extractBrokerFee(String text) {

        Matcher matcher = Pattern.compile(
                "중개보수\\s*([\\d,]+)"
        ).matcher(text);

        if (matcher.find()) {

            return parseMoney(matcher.group(1));
        }

        return null;
    }
}
