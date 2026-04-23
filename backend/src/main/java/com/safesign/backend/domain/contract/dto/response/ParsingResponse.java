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

    private HeaderResponse header;
    private List<ClauseResponse> clauses;
    private List<PartyResponse> parties;
    private List<AgentResponse> agents;
    private String contractDate;
}