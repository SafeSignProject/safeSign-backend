package com.safesign.backend.domain.contract.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HeaderResponse {

    private String address;
    private String landCategory;
    private String landArea;

    private String buildingStructure;
    private String buildingUsage;
    private String buildingArea;

    private String leasedPart;
    private String leasedArea;

    private String deposit;
    private String contractAmount;
    private String middlePayment;
    private String balance;
    private String monthlyRent;
}