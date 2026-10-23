package com.tech.emailvalidation.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.tech.emailvalidation.dto.ApiResponse;
import com.tech.emailvalidation.dto.EmailDeliverability;
import com.tech.emailvalidation.dto.EmailValidationResponse;
import com.tech.emailvalidation.dto.VerifyResult;

@Service
public class EmailValidationServiceImpl implements EmailValidationService {

    private final WebClient webClient;

    @Value("${abstract.api.key}")
    private String apiKey;

    @Value("${abstract.api.base-url}")
    private String baseUrl;

    // Standard SMTP Response Codes
    private static final int SMTP_SUCCESS_CODE = 250;
    private static final int SMTP_FAILURE_CODE = 550;
    private static final int SMTP_ERROR_CODE = 500;

    public EmailValidationServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public ApiResponse validateEmail(String email) {
        try {
            // Build URI dynamically using baseUrl from application.yml
            String requestUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("api_key", apiKey.trim())
                    .queryParam("email", email.trim())
                    .toUriString();

            EmailValidationResponse response = webClient.get()
                    .uri(requestUrl)
                    .retrieve()
                    .bodyToMono(EmailValidationResponse.class)
                    .block();

            // Extract EmailDeliverability safely using Optional
            Optional<EmailDeliverability> deliverabilityOpt = Optional.ofNullable(response)
                    .map(r -> r.getEmailDeliverability());

            boolean isFormatValid = deliverabilityOpt
                    .map(deliverability -> deliverability.getFormatValid())
                    .orElse(false);

            boolean isMxValid = deliverabilityOpt
                    .map(EmailDeliverability::getMxValid)
                    .orElse(false);

            boolean isSmtpValid = deliverabilityOpt
                    .map(EmailDeliverability::getSmtpValid)
                    .orElse(false);

            // Overall email validity rule
            boolean isValid = isFormatValid && isMxValid && isSmtpValid;

            // Extract primary MX record safely
            String mxRecord = deliverabilityOpt
                    .map(EmailDeliverability::getMxRecords)
                    .filter(records -> !records.isEmpty())
                    .map(records -> records.get(0))
                    .orElse(null);

            // Dynamic SMTP code selection
            int statusCode = isValid ? SMTP_SUCCESS_CODE : SMTP_FAILURE_CODE;

            VerifyResult verifyResult = new VerifyResult(
                    email,
                    isFormatValid,
                    isMxValid,
                    isSmtpValid,
                    mxRecord,
                    statusCode,
                    String.valueOf(statusCode)
            );

            return new ApiResponse(
                    verifyResult,
                    isValid ? "Success" : "Failed",
                    isValid
            );

        } catch (Exception e) {
            VerifyResult verifyResult = new VerifyResult(
                    email,
                    false,
                    false,
                    false,
                    null,
                    SMTP_ERROR_CODE,
                    String.valueOf(SMTP_ERROR_CODE)
            );

            return new ApiResponse(
                    verifyResult,
                    "Error: " + e.getMessage(),
                    false
            );
        }
    }
}