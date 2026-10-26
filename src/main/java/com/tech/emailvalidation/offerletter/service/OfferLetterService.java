package com.tech.emailvalidation.offerletter.service;

import java.util.List;

import com.tech.emailvalidation.offerletter.dto.OfferLetterRequest;
import com.tech.emailvalidation.offerletter.dto.OfferLetterResponse;
import com.tech.emailvalidation.offerletter.entity.OfferLetter;

public interface OfferLetterService {

    /**
     * Generates an Offer Letter PDF from request details, sends the congratulatory
     * email with the PDF attached, and persists the record to the database.
     *
     * @param request candidate and job details
     * @return response with generation and email dispatch results
     */
    OfferLetterResponse generateAndSendOfferLetter(OfferLetterRequest request);

    /**
     * Generates only the PDF bytes without sending email or saving (for direct preview/streaming).
     *
     * @param request candidate and job details
     * @return PDF byte array
     */
    byte[] generatePdfOnly(OfferLetterRequest request);

    /**
     * Retrieves an OfferLetter entity by its database ID.
     */
    OfferLetter getOfferLetterById(Long id);

    /**
     * Retrieves the stored PDF byte array for a generated offer letter by ID.
     */
    byte[] getOfferLetterPdfById(Long id);

    /**
     * Lists all generated offer letters.
     */
    List<OfferLetterResponse> getAllOfferLetters();
}
