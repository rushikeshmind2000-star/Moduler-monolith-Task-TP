package com.tech.emailvalidation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailDeliverability {

    private String status;

    @JsonProperty("status_detail")
    private String statusDetail;

    @JsonProperty("is_format_valid")
    private Boolean formatValid;

    @JsonProperty("is_smtp_valid")
    private Boolean smtpValid;

    @JsonProperty("is_mx_valid")
    private Boolean mxValid;

    @JsonProperty("mx_records")
    private List<String> mxRecords;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDetail() {
        return statusDetail;
    }

    public void setStatusDetail(String statusDetail) {
        this.statusDetail = statusDetail;
    }

    public Boolean getFormatValid() {
        return formatValid;
    }

    public void setFormatValid(Boolean formatValid) {
        this.formatValid = formatValid;
    }

    public Boolean getSmtpValid() {
        return smtpValid;
    }

    public void setSmtpValid(Boolean smtpValid) {
        this.smtpValid = smtpValid;
    }

    public Boolean getMxValid() {
        return mxValid;
    }

    public void setMxValid(Boolean mxValid) {
        this.mxValid = mxValid;
    }

    public List<String> getMxRecords() {
        return mxRecords;
    }

    public void setMxRecords(List<String> mxRecords) {
        this.mxRecords = mxRecords;
    }
}