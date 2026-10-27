package com.tech.emailvalidation.service;

import com.tech.emailvalidation.dto.EmailVerificationResult;

public interface EmailVerifierService {

    /**
     * Performs fast multi-stage email validation:
     * 1. RFC 5322 syntax validation
     * 2. Disposable / temporary email check
     * 3. DNS MX record resolution (with in-memory cache)
     */
    EmailVerificationResult verifyEmail(String email);

    /**
     * Validates candidate email address for offer letter generation.
     * Throws EmailValidationException if format is invalid, disposable, or domain has no mail servers.
     *
     * @param email candidate email to validate
     */
    void validateCandidateEmail(String email);
}
