package com.tech.emailvalidation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BreachedDomain {

    private String domain;

    @JsonProperty("date_breached")
    private String dateBreached;

    public BreachedDomain() {
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDateBreached() {
        return dateBreached;
    }

    public void setDateBreached(String dateBreached) {
        this.dateBreached = dateBreached;
    }
}