package com.tech.emailvalidation.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tech.emailvalidation.dto.EmailVerificationResult;
import com.tech.emailvalidation.exception.EmailValidationException;

class EmailVerifierServiceTest {

    private EmailVerifierServiceImpl emailVerifierService;

    @BeforeEach
    void setUp() {
        emailVerifierService = new EmailVerifierServiceImpl();
    }

    @Test
    @DisplayName("Should validate valid email on known public domains like gmail.com")
    void testValidEmail() {
        EmailVerificationResult result = emailVerifierService.verifyEmail("mindrushi55@gmail.com");
        assertNotNull(result);
        assertTrue(result.isFormatValid(), "Format should be valid");
        assertTrue(result.isMxValid(), "gmail.com must have valid MX records");
        assertFalse(result.isDisposable(), "gmail.com is not disposable");
        assertTrue(result.isValid(), "Email should be overall valid");
    }

    @Test
    @DisplayName("Should detect invalid email syntax")
    void testInvalidSyntax() {
        EmailVerificationResult result = emailVerifierService.verifyEmail("invalid-email-no-at-sign");
        assertNotNull(result);
        assertFalse(result.isValid());
        assertFalse(result.isFormatValid());

        EmailVerificationResult result2 = emailVerifierService.verifyEmail("test@.com");
        assertNotNull(result2);
        assertFalse(result2.isValid());
    }

    @Test
    @DisplayName("Should detect disposable / temporary email domains")
    void testDisposableDomain() {
        EmailVerificationResult result = emailVerifierService.verifyEmail("tempuser@mailinator.com");
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.isDisposable());
        assertTrue(result.getFailureReason().contains("Disposable or temporary"));
    }

    @Test
    @DisplayName("Should reject nonexistent domain without MX records")
    void testNonexistentDomain() {
        EmailVerificationResult result = emailVerifierService.verifyEmail("user@nonexistentdomain123987123987123.com");
        assertNotNull(result);
        assertFalse(result.isValid());
        assertFalse(result.isMxValid());
    }

    @Test
    @DisplayName("validateCandidateEmail should throw EmailValidationException for invalid emails")
    void testValidateCandidateEmailException() {
        assertThrows(EmailValidationException.class, () ->
                emailVerifierService.validateCandidateEmail("invalid-email")
        );
        assertThrows(EmailValidationException.class, () ->
                emailVerifierService.validateCandidateEmail("hacker@mailinator.com")
        );
    }
}
