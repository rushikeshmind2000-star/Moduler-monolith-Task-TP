package com.tech.emailvalidation.service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.tech.emailvalidation.dto.ApiResponse;
import com.tech.emailvalidation.dto.EmailDeliverability;
import com.tech.emailvalidation.dto.EmailValidationResponse;
import com.tech.emailvalidation.dto.EmailVerificationResult;
import com.tech.emailvalidation.dto.VerifyResult;

@Service
public class EmailValidationServiceImpl implements EmailValidationService {

    private static final Logger log = LoggerFactory.getLogger(EmailValidationServiceImpl.class);

    private final WebClient webClient;
    private final EmailVerifierService emailVerifierService;

    @Value("${abstract.api.key:}")
    private String apiKey;

    @Value("${abstract.api.base-url:}")
    private String baseUrl;

    // Standard SMTP Response Codes
    private static final int SMTP_SUCCESS_CODE = 250;
    private static final int SMTP_FAILURE_CODE = 550;
    private static final int SMTP_ERROR_CODE = 500;

    // In-memory cache for validated emails to optimize API response time to ~0ms
    private final Map<String, CachedApiResponse> responseCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 6 * 60 * 60 * 1000L; // 6 hours

    private static class CachedApiResponse {
        final ApiResponse apiResponse;
        final long timestamp;

        CachedApiResponse(ApiResponse apiResponse) {
            this.apiResponse = apiResponse;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > CACHE_TTL_MS;
        }
    }

    public EmailValidationServiceImpl(WebClient webClient, EmailVerifierService emailVerifierService) {
        this.webClient = webClient;
        this.emailVerifierService = emailVerifierService;
    }

    @Override
    public ApiResponse validateEmail(String email) {
        if (email == null || email.isBlank()) {
            VerifyResult verifyResult = new VerifyResult(
                    "",
                    false,
                    false,
                    false,
                    null,
                    SMTP_FAILURE_CODE,
                    String.valueOf(SMTP_FAILURE_CODE)
            );
            return new ApiResponse(verifyResult, "Email cannot be empty", false);
        }

        String normalizedEmail = email.trim().toLowerCase();

        // Check in-memory cache first for sub-millisecond response
        CachedApiResponse cached = responseCache.get(normalizedEmail);
        if (cached != null && !cached.isExpired()) {
            log.debug("Returning cached email validation response for {}", normalizedEmail);
            return cached.apiResponse;
        }

        // Fast local pre-validation: RFC syntax, disposable domain, and DNS MX checks
        EmailVerificationResult localResult = emailVerifierService.verifyEmail(normalizedEmail);
        if (!localResult.isValid()) {
            VerifyResult verifyResult = new VerifyResult(
                    normalizedEmail,
                    localResult.isFormatValid(),
                    localResult.isMxValid(),
                    false,
                    localResult.getPrimaryMx(),
                    SMTP_FAILURE_CODE,
                    String.valueOf(SMTP_FAILURE_CODE)
            );
            ApiResponse apiResponse = new ApiResponse(verifyResult, localResult.getFailureReason(), false);
            responseCache.put(normalizedEmail, new CachedApiResponse(apiResponse));
            return apiResponse;
        }

        // Attempt AbstractAPI verification with tight timeout and graceful fallback
        if (apiKey != null && !apiKey.isBlank() && baseUrl != null && !baseUrl.isBlank()) {
            try {
                String requestUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .queryParam("api_key", apiKey.trim())
                        .queryParam("email", normalizedEmail)
                        .toUriString();

                EmailValidationResponse response = webClient.get()
                        .uri(requestUrl)
                        .retrieve()
                        .bodyToMono(EmailValidationResponse.class)
                        .timeout(Duration.ofMillis(2500))
                        .block();

                Optional<EmailDeliverability> deliverabilityOpt = Optional.ofNullable(response)
                        .map(EmailValidationResponse::getEmailDeliverability);

                boolean isFormatValid = deliverabilityOpt
                        .map(EmailDeliverability::getFormatValid)
                        .orElse(localResult.isFormatValid());

                boolean isMxValid = deliverabilityOpt
                        .map(EmailDeliverability::getMxValid)
                        .orElse(localResult.isMxValid());

                boolean isSmtpValid = deliverabilityOpt
                        .map(EmailDeliverability::getSmtpValid)
                        .orElse(true);

                boolean isValid = isFormatValid && isMxValid && isSmtpValid;

                String mxRecord = deliverabilityOpt
                        .map(EmailDeliverability::getMxRecords)
                        .filter(records -> !records.isEmpty())
                        .map(records -> records.get(0))
                        .orElse(localResult.getPrimaryMx());

                int statusCode = isValid ? SMTP_SUCCESS_CODE : SMTP_FAILURE_CODE;

                VerifyResult verifyResult = new VerifyResult(
                        normalizedEmail,
                        isFormatValid,
                        isMxValid,
                        isSmtpValid,
                        mxRecord,
                        statusCode,
                        String.valueOf(statusCode)
                );

                ApiResponse apiResponse = new ApiResponse(
                        verifyResult,
                        isValid ? "Success" : "Failed",
                        isValid
                );

                responseCache.put(normalizedEmail, new CachedApiResponse(apiResponse));
                return apiResponse;

            } catch (Exception e) {
                log.warn("External validation API call failed or timed out for {}: {}. Falling back to DNS MX verification.",
                        normalizedEmail, e.getMessage());
            }
        }

        // Graceful fallback to verified local DNS MX result
        boolean isValid = localResult.isFormatValid() && localResult.isMxValid();
        int statusCode = isValid ? SMTP_SUCCESS_CODE : SMTP_FAILURE_CODE;

        VerifyResult verifyResult = new VerifyResult(
                normalizedEmail,
                localResult.isFormatValid(),
                localResult.isMxValid(),
                localResult.isMxValid(),
                localResult.getPrimaryMx(),
                statusCode,
                String.valueOf(statusCode)
        );

        ApiResponse fallbackResponse = new ApiResponse(
                verifyResult,
                isValid ? "Success" : "Failed: " + localResult.getFailureReason(),
                isValid
        );

        responseCache.put(normalizedEmail, new CachedApiResponse(fallbackResponse));
        return fallbackResponse;
    }
}