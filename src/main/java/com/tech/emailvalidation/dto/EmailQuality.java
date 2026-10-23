package com.tech.emailvalidation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailQuality {

    private Double score;

    @JsonProperty("is_free_email")
    private Boolean freeEmail;

    @JsonProperty("is_username_suspicious")
    private Boolean usernameSuspicious;

    @JsonProperty("is_disposable")
    private Boolean disposable;

    @JsonProperty("is_catchall")
    private Boolean catchall;

    @JsonProperty("is_subaddress")
    private Boolean subaddress;

    @JsonProperty("is_role")
    private Boolean role;

    @JsonProperty("is_dmarc_enforced")
    private Boolean dmarcEnforced;

    @JsonProperty("is_spf_strict")
    private Boolean spfStrict;

    @JsonProperty("minimum_age")
    private Integer minimumAge;
}