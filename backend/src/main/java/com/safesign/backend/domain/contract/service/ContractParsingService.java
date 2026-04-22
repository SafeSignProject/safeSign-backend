package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.*;
import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.contract.entity.ContractOcrResult;
import com.safesign.backend.domain.contract.repository.ContractRepository;
import com.safesign.backend.domain.contract.repository.ContractOcrResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.*;

@Service
@RequiredArgsConstructor
public class ContractParsingService {

    private final ContractRepository contractRepository;
    private final ContractOcrResultRepository ocrRepository;

    @Transactional(readOnly = true)
    public ParsingResponse parse(Long contractId) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("계약 없음"));

        ContractOcrResult ocr = ocrRepository
                .findTopByContract_ContractIdOrderByOcrResultIdDesc(contractId)
                .orElseThrow(() -> new RuntimeException("OCR 없음"));

        String text = preprocess(ocr.getFullText());

        List<String> lines = toLines(text);
        List<String> headerLines = extractHeaderLines(lines);

        HeaderResponse header = parseHeader(headerLines);
        List<ClauseResponse> clauses = parseClauses(text);
        List<PartyResponse> parties = parseParties(lines);
        List<AgentResponse> agents = parseAgents(lines);
        String contractDate = extractContractDate(text);

        return new ParsingResponse(
                contractId,
                clauses.size(),
                "파싱 완료",
                header,
                clauses,
                parties,
                agents,
                contractDate
        );
    }

    // =====================
    // 전처리
    // =====================
    private String preprocess(String text) {
        return text
                .replaceAll(":unselected:", "")
                .replaceAll("\\r", "")

                .replaceAll("건\\s*물", "건물")
                .replaceAll("토\\s*지", "토지")
                .replaceAll("구\\s*조", "구조")
                .replaceAll("용\\s*도", "용도")

                .replaceAll("(제\\d+조)", "\n$1\n")

                // 숫자 노이즈 제거
                .replaceAll("(\\d+\\.\\d+)", "")
                .replaceAll("(\\d{3,}\\.)", "")

                .replaceAll("\\n+", "\n")
                .trim();
    }

    private List<String> toLines(String text) {
        return Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // =====================
    // HEADER 분리
    // =====================
    private List<String> extractHeaderLines(List<String> lines) {
        List<String> header = new ArrayList<>();
        for (String line : lines) {
            if (line.matches("제\\d+조.*")) break;
            header.add(line);
        }
        return header;
    }

    // =====================
    // HEADER 파싱
    // =====================
    private HeaderResponse parseHeader(List<String> lines) {

        String address = null;
        String landCategory = null;
        String landArea = null;
        String buildingStructure = null;
        String buildingUsage = null;
        String buildingArea = null;
        String leasedPart = null;
        String leasedArea = null;

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);

            if (line.equals("소재지")) address = safe(lines, i + 1);

            if (line.contains("지목"))
                landCategory = line.replaceAll("[^가-힣]", "");

            if (line.equals("면적") && landArea == null)
                landArea = safe(lines, i + 1);

            if (line.equals("구조"))
                buildingStructure = safe(lines, i + 1);

            if (line.equals("용도")) {
                String next = safe(lines, i + 1);
                if (next != null && !next.matches("제\\d+조.*")) {
                    buildingUsage = next;
                }
            }

            if (line.equals("면적") && i > 0 && lines.get(i - 1).contains("용도")) {
                buildingArea = safe(lines, i + 1);
            }

            if (line.contains("임대부분"))
                leasedPart = line.replace("임대부분", "").trim();

            if (line.contains("약") && line.contains("m2"))
                leasedArea = line;
        }

        return new HeaderResponse(
                address,
                landCategory,
                landArea,
                buildingStructure,
                buildingUsage,
                buildingArea,
                leasedPart,
                leasedArea,
                null, null, null, null, null
        );
    }

    // =====================
    // 🔥 조항 파싱 (핵심)
    // =====================
    private List<ClauseResponse> parseClauses(String text) {

        List<ClauseResponse> result = new ArrayList<>();
        int order = 1;

        String[] parts = text.split("특약사항");
        String body = parts[0];
        String special = parts.length > 1 ? parts[1] : "";

        // 제N조
        Pattern p = Pattern.compile("(제\\d+조)(.*?)(?=제\\d+조|$)", Pattern.DOTALL);
        Matcher m = p.matcher(body);

        while (m.find()) {
            result.add(new ClauseResponse(
                    m.group(1),
                    m.group(2).trim(),
                    order++
            ));
        }

        // 특약
        Pattern sp = Pattern.compile("(\\d+\\.)(.*?)(?=\\d+\\.|$)", Pattern.DOTALL);
        Matcher sm = sp.matcher(special);

        while (sm.find()) {

            String title = sm.group(1);
            String content = sm.group(2).trim();

            // 🔥 6번에서 하단 제거
            if (title.equals("6.")) {
                int idx = content.indexOf("임대인");
                if (idx > 0) {
                    content = content.substring(0, idx);
                }
            }

            if (content.length() < 10) continue;

            result.add(new ClauseResponse(title, content, order++));
        }

        return result;
    }

    // =====================
    // 당사자
    // =====================
    private List<PartyResponse> parseParties(List<String> lines) {

        String landlordName = null, landlordPhone = null, landlordAddress = null, landlordRRN = null;
        String tenantName = null, tenantPhone = null, tenantAddress = null, tenantRRN = null;

        for (int i = 0; i < lines.size(); i++) {

            if (lines.get(i).contains("임대인")) {
                landlordName = findNext(lines, i, "성 명");
                landlordPhone = findPhone(lines, i);
                landlordAddress = findNext(lines, i, "주 소");
                landlordRRN = findRRN(lines, i);
            }

            if (lines.get(i).contains("임차인")) {
                tenantName = findNext(lines, i, "성 명");
                tenantPhone = findPhone(lines, i);
                tenantAddress = findNext(lines, i, "주 소");
                tenantRRN = findRRN(lines, i);
            }
        }

        return List.of(new PartyResponse(
                landlordName, landlordPhone, landlordAddress, landlordRRN,
                tenantName, tenantPhone, tenantAddress, tenantRRN
        ));
    }

    // =====================
    // 중개사
    // =====================
    private List<AgentResponse> parseAgents(List<String> lines) {

        List<AgentResponse> list = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("공인중개사")) {

                list.add(new AgentResponse(
                        findNext(lines, i, "사무소 명칭"),
                        findNext(lines, i, "소재지"),
                        findPhone(lines, i),
                        findNext(lines, i, "등록 번 호")
                ));
            }
        }

        return list;
    }

    private String extractContractDate(String text) {
        Matcher m = Pattern.compile("\\d{4}년\\s*\\d{2}월\\s*\\d{2}일").matcher(text);
        return m.find() ? m.group() : null;
    }

    // =====================
    // 유틸
    // =====================
    private String safe(List<String> lines, int idx) {
        return idx < lines.size() ? lines.get(idx) : null;
    }

    private String findNext(List<String> lines, int start, String key) {
        for (int i = start; i < lines.size(); i++) {
            if (lines.get(i).contains(key) && i + 1 < lines.size()) {
                return lines.get(i + 1);
            }
        }
        return null;
    }

    private String findPhone(List<String> lines, int start) {
        for (int i = start; i < lines.size(); i++) {
            if (lines.get(i).matches("010-\\d{4}-\\d{4}")) {
                return lines.get(i);
            }
        }
        return null;
    }

    private String findRRN(List<String> lines, int start) {
        for (int i = start; i < lines.size(); i++) {
            if (lines.get(i).matches("\\d{6}-\\d{7}")) {
                return lines.get(i);
            }
        }
        return null;
    }
}