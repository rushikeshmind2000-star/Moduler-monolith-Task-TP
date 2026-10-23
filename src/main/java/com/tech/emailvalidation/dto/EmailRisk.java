package com.tech.emailvalidation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailRisk {

    @JsonProperty("address_risk_status")
    private String addressRiskStatus;

    @JsonProperty("domain_risk_status")
    private String domainRiskStatus;
}