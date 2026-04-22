package com.safesign.backend.domain.contract.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgentResponse {

    private String officeName;
    private String address;
    private String phone;
    private String licenseNumber;
}