package com.tech.emailvalidation.offerletter.service;

import com.tech.emailvalidation.offerletter.dto.EmailDispatchResult;

public interface EmailSenderService {

    /**
     * Sends the formal job offer email with the generated PDF attached.
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
}
