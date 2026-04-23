package com.safesign.backend.domain.contract.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PartyResponse {

    private String landlordName;
    private String landlordPhone;
    private String landlordAddress;
    private String landlordRRN;

    private String tenantName;
    private String tenantPhone;
    private String tenantAddress;
    private String tenantRRN;
}