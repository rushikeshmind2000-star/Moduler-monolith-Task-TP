package com.tech.emailvalidation.offerletter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tech.emailvalidation.exception.EmailValidationException;
import com.tech.emailvalidation.offerletter.config.OfferLetterProperties;
import com.tech.emailvalidation.offerletter.dto.EmailDispatchResult;
import com.tech.emailvalidation.offerletter.dto.OfferLetterRequest;
import com.tech.emailvalidation.offerletter.dto.OfferLetterResponse;
import com.tech.emailvalidation.offerletter.entity.OfferLetter;
import com.tech.emailvalidation.offerletter.repository.OfferLetterRepository;
import com.tech.emailvalidation.offerletter.service.EmailSenderService;
import com.tech.emailvalidation.offerletter.service.OfferLetterServiceImpl;
import com.tech.emailvalidation.offerletter.service.PdfGenerationServiceImpl;
import com.tech.emailvalidation.service.EmailVerifierService;

@ExtendWith(MockitoExtension.class)
class OfferLetterServiceTest {

    private OfferLetterProperties properties;
    private PdfGenerationServiceImpl pdfGenerationService;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private OfferLetterRepository offerLetterRepository;

    @Mock
    private EmailVerifierService emailVerifierService;

    private OfferLetterServiceImpl offerLetterService;

    @BeforeEach
    void setUp() {
        properties = new OfferLetterProperties();
        pdfGenerationService = new PdfGenerationServiceImpl(properties);
        offerLetterService = new OfferLetterServiceImpl(
                pdfGenerationService,
                emailSenderService,
                offerLetterRepository,
                properties,
                emailVerifierService
        );
    }

    @Test
    @DisplayName("Should successfully generate a valid PDF document with %PDF magic header")
    void testPdfGeneration() {
        OfferLetterRequest request = OfferLetterRequest.builder()
                .employeeName("Rushikesh Shinde")
                .email("rushikesh@example.com")
                .address("Flat 402, Skyline Towers, Hinjewadi, Pune 411057")
                .packageAmount("12 LPA")
                .designation("Senior Java Developer")
                .department("Engineering")
                .joiningDate("2026-11-15")
                .build();

        byte[] pdfBytes = pdfGenerationService.generateOfferLetterPdf(request, "TP-OFFER-2026-1001");

        assertNotNull(pdfBytes, "Generated PDF bytes should not be null");
        assertTrue(pdfBytes.length > 500, "PDF should have substantial content size");

        // Verify PDF Header magic bytes "%PDF"
        String header = new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII);
        assertEquals("%PDF", header, "Generated file must be a valid PDF starting with %PDF");

        // Verify that the Offer Letter is strictly 1 single page
        try {
            com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(pdfBytes);
            assertEquals(1, reader.getNumberOfPages(), "Offer letter must fit onto a single page (1 page)");
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read PDF page count", e);
        }
    }

    @Test
    @DisplayName("Should generate offer letter, dispatch async email in background, and persist record")
    void testGenerateAndSendOfferLetterAsync() {
        OfferLetterRequest request = OfferLetterRequest.builder()
                .employeeName("Jane Doe")
                .email("jane.doe@example.com")
                .address("456 Innovation Park, Bangalore 560100")
                .packageAmount("18 LPA")
                .designation("Lead Backend Architect")
                .sendEmail(true)
                .asyncEmail(true)
                .build();

        doNothing().when(emailVerifierService).validateCandidateEmail("jane.doe@example.com");

        when(offerLetterRepository.save(any(OfferLetter.class))).thenAnswer(invocation -> {
            OfferLetter entity = invocation.getArgument(0);
            entity.setId(101L);
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        });

        when(emailSenderService.sendOfferLetterEmailAsync(
                eq(101L), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(byte[].class), anyString()
        )).thenReturn(CompletableFuture.completedFuture(EmailDispatchResult.ok("Email delivered successfully")));

        OfferLetterResponse response = offerLetterService.generateAndSendOfferLetter(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Jane Doe", response.getEmployeeName());
        assertEquals("jane.doe@example.com", response.getEmail());
        assertEquals("18 LPA", response.getPackageAmount());
        assertEquals("Lead Backend Architect", response.getDesignation());
        assertTrue(response.getEmailSent());
        assertEquals("SUCCESS", response.getStatus());

        verify(emailVerifierService).validateCandidateEmail("jane.doe@example.com");
        verify(emailSenderService).sendOfferLetterEmailAsync(
                eq(101L), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(byte[].class), anyString()
        );
        verify(offerLetterRepository).save(any(OfferLetter.class));
    }

    @Test
    @DisplayName("Should reject offer letter generation when candidate email is invalid")
    void testRejectInvalidEmail() {
        OfferLetterRequest request = OfferLetterRequest.builder()
                .employeeName("Bad Candidate")
                .email("bad-candidate@invalid-domain-xyz.com")
                .address("Pune")
                .packageAmount("10 LPA")
                .build();

        doThrow(new EmailValidationException("Domain does not have valid mail exchange (MX) servers"))
                .when(emailVerifierService).validateCandidateEmail("bad-candidate@invalid-domain-xyz.com");

        EmailValidationException ex = assertThrows(EmailValidationException.class, () ->
                offerLetterService.generateAndSendOfferLetter(request)
        );

        assertTrue(ex.getMessage().contains("mail exchange (MX) servers"));
    }
}
