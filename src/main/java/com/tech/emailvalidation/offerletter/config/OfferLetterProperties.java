package com.tech.emailvalidation.offerletter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "offer-letter")
@Getter
@Setter
public class OfferLetterProperties {

    private Company company = new Company();
    private Hr hr = new Hr();

    @Getter
    @Setter
    public static class Company {
        private String name = "ABC Technologies Pvt. Ltd.";
        private String address = "123 Business Park, Pune, Maharashtra – 411001";
        private String email = "hr@abctechnologies.com";
        private String website = "www.abctechnologies.com";
        private String phone = "+91 98765 43210";
    }

    @Getter
    @Setter
    public static class Hr {
        private String senderName = "Human Resources Department";
        private String senderEmail = "hr@abctechnologies.com";
        private String signatoryName = "Priya Deshmukh";
        private String signatoryTitle = "Human Resources Manager";
        private String signatoryPhone = "+91 98765 43210";
        private String signatoryEmail = "hr@abctechnologies.com";
    }
}
