package com.tech.emailvalidation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailValidationResponse {

    @JsonProperty("email_address")
    private String emailAddress;

    @JsonProperty("suggested_correction")
    private String suggestedCorrection;

    @JsonProperty("email_deliverability")
    private EmailDeliverability emailDeliverability;

    @JsonProperty("email_quality")
    private EmailQuality emailQuality;

    @JsonProperty("email_sender")
    private EmailSender emailSender;

    @JsonProperty("email_domain")
    private EmailDomain emailDomain;

    @JsonProperty("email_risk")
    private EmailRisk emailRisk;

    @JsonProperty("email_breaches")
    private EmailBreaches emailBreaches;

    public EmailValidationResponse() {
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getSuggestedCorrection() {
        return suggestedCorrection;
    }

    public void setSuggestedCorrection(String suggestedCorrection) {
        this.suggestedCorrection = suggestedCorrection;
    }

    public EmailDeliverability getEmailDeliverability() {
        return emailDeliverability;
    }

    public void setEmailDeliverability(EmailDeliverability emailDeliverability) {
        this.emailDeliverability = emailDeliverability;
    }

    public EmailQuality getEmailQuality() {
        return emailQuality;
    }

    public void setEmailQuality(EmailQuality emailQuality) {
        this.emailQuality = emailQuality;
    }

    public EmailSender getEmailSender() {
        return emailSender;
    }

    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public EmailDomain getEmailDomain() {
        return emailDomain;
    }

    public void setEmailDomain(EmailDomain emailDomain) {
        this.emailDomain = emailDomain;
    }

    public EmailRisk getEmailRisk() {
        return emailRisk;
    }

    public void setEmailRisk(EmailRisk emailRisk) {
        this.emailRisk = emailRisk;
    }

    public EmailBreaches getEmailBreaches() {
        return emailBreaches;
    }

    public void setEmailBreaches(EmailBreaches emailBreaches) {
        this.emailBreaches = emailBreaches;
    }
}