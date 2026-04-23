package com.safesign.backend.domain.contract.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClauseResponse {

    private String title;
    private String content;
    private Integer orderNo;
}