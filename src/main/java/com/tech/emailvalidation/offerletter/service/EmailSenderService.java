package com.tech.emailvalidation.offerletter.service;

import java.util.concurrent.CompletableFuture;

import com.tech.emailvalidation.offerletter.dto.EmailDispatchResult;

public interface EmailSenderService {

    /**
     * Sends the formal job offer email with the generated PDF attached synchronously.
     */
    EmailDispatchResult sendOfferLetterEmail(
            String recipientEmail,
            String recipientName,
            String designation,
            String department,
            String packageAmount,
            String joiningDate,
            String address,
            String companyName,
            byte[] pdfBytes,
            String pdfFileName
    );

    /**
     * Sends the formal job offer email asynchronously in the background.
     * Updates the OfferLetter database record status once delivery completes.
     */
    CompletableFuture<EmailDispatchResult> sendOfferLetterEmailAsync(
            Long offerLetterId,
            String recipientEmail,
            String recipientName,
            String designation,
            String department,
            String packageAmount,
            String joiningDate,
            String address,
            String companyName,
            byte[] pdfBytes,
            String pdfFileName
    );
}
