package com.safesign.backend.domain.contract.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ParsingResponse {

    private Long contractId;
    private int clauseCount;
    private String message;

    // OCR 텍스트를 줄 단위로 분리한 전체 문장 리스트
    private List<String> lines;

    // 분리된 조항(제N조, 특약사항) 리스트
    private List<ClauseResponse> clauses;

    // 좌표 기반 추출 로직 도입 -> 표제부 추출
    private HeaderResponse header;

    // ---------------------------------------------------------
    // 아직 복구하지 않은 당사자, 중개사, 계약일 등은 삭제 유지
    // ---------------------------------------------------------
    // private List<PartyResponse> parties;
    // private List<AgentResponse> agents;
    // private String contractDate;
}