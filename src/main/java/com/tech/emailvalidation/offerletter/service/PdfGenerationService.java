package com.tech.emailvalidation.offerletter.service;

import com.tech.emailvalidation.offerletter.dto.OfferLetterRequest;

public interface PdfGenerationService {

    /**
     * Generates a professional PDF offer letter and returns the byte array.
     *
     * @param request candidate and job details
     * @param referenceNumber unique offer reference ID
     * @return PDF content as byte array
     */
    byte[] generateOfferLetterPdf(OfferLetterRequest request, String referenceNumber);
}
