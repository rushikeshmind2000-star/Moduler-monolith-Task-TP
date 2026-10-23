package com.tech.emailvalidation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailDomain {

    private String domain;

    @JsonProperty("domain_age")
    private Integer domainAge;

    @JsonProperty("is_live_site")
    private Boolean liveSite;

    private String registrar;

    @JsonProperty("registrar_url")
    private String registrarUrl;

    @JsonProperty("date_registered")
    private String dateRegistered;

    @JsonProperty("date_last_renewed")
    private String dateLastRenewed;

    @JsonProperty("date_expires")
    private String dateExpires;

    @JsonProperty("is_risky_tld")
    private Boolean riskyTld;
}