package com.tech.emailvalidation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailBreaches {

    @JsonProperty("total_breaches")
    private Integer totalBreaches;

    @JsonProperty("date_first_breached")
    private String dateFirstBreached;

    @JsonProperty("date_last_breached")
    private String dateLastBreached;

    @JsonProperty("breached_domains")
    private List<BreachedDomain> breachedDomains;
}