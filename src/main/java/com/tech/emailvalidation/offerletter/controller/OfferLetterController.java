package com.tech.emailvalidation.offerletter.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tech.emailvalidation.offerletter.dto.OfferLetterRequest;
import com.tech.emailvalidation.offerletter.dto.OfferLetterResponse;
import com.tech.emailvalidation.offerletter.entity.OfferLetter;
import com.tech.emailvalidation.offerletter.service.OfferLetterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/offer-letter")
@Tag(name = "Offer Letter Module", description = "Automated Offer Letter PDF Generation and Candidate Email Notification")
public class OfferLetterController {

    private static final Logger log = LoggerFactory.getLogger(OfferLetterController.class);

    private final OfferLetterService offerLetterService;

    public OfferLetterController(OfferLetterService offerLetterService) {
        this.offerLetterService = offerLetterService;
    }

    /**
     * Primary endpoint:
     * Receives employeeName, mail/email, address, package and other details,
     * generates a professional offer letter PDF, attaches it to a congratulatory email,
     * sends it to the candidate, and saves the record. Returns clean JSON.
     */
    @PostMapping("/generate-and-send")
    @Operation(
            summary = "1. Generate Offer Letter & Send Email (Returns JSON Response)",
            description = "Validates candidate email (format, disposable check, DNS MX), fills candidate details into Offer Letter PDF, dispatches email (async in background by default for sub-second API latency), saves record to DB, and returns a JSON response."
    )
    @ApiResponse(responseCode = "201", description = "Offer letter generated, email dispatched/queued, and record saved (returns JSON)")
    @ApiResponse(responseCode = "400", description = "Validation error on required fields or invalid/undeliverable candidate email")
    public ResponseEntity<OfferLetterResponse> generateAndSendOfferLetter(
            @Valid @RequestBody OfferLetterRequest request,
            @org.springframework.web.bind.annotation.RequestParam(value = "async", required = false) Boolean asyncParam) {
        if (asyncParam != null) {
            request.setAsyncEmail(asyncParam);
        }
        log.info("API request received: generate-and-send offer letter for {} (async={})", request.getEmployeeName(), request.getAsyncEmail());
        OfferLetterResponse response = offerLetterService.generateAndSendOfferLetter(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Preview/direct PDF download endpoint:
     * Generates and returns the PDF directly as binary stream without requiring email dispatch.
     */
    @PostMapping(value = "/generate-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "2. Generate and Download PDF File Directly (Binary Stream)",
            description = "Returns the generated Offer Letter PDF directly as a downloadable binary file. Click the 'Download file' link in Swagger UI to view the PDF."
    )
    @ApiResponse(
            responseCode = "200",
            description = "PDF document generated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))
    )
    public ResponseEntity<byte[]> generatePdfOnly(@Valid @RequestBody OfferLetterRequest request) {
        log.info("API request received: generate-pdf only for {}", request.getEmployeeName());
        byte[] pdfBytes = offerLetterService.generatePdfOnly(request);

        String filename = "Offer_Letter_" + sanitizeFileName(request.getEmployeeName()) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }

    /**
     * Downloads an already generated offer letter PDF by its database ID.
     */
    @GetMapping(value = "/{id}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Download Saved Offer Letter PDF by ID",
            description = "Fetches and downloads the generated PDF for an existing offer letter from MySQL."
    )
    @ApiResponse(
            responseCode = "200",
            description = "PDF document downloaded successfully",
            content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))
    )
    public ResponseEntity<byte[]> downloadPdfById(@PathVariable Long id) {
        OfferLetter offerLetter = offerLetterService.getOfferLetterById(id);
        byte[] pdfBytes = offerLetterService.getOfferLetterPdfById(id);

        String filename = "Offer_Letter_" + sanitizeFileName(offerLetter.getEmployeeName()) + "_" + offerLetter.getReferenceNumber() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }

    /**
     * Get offer letter record details by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Offer Letter Details by ID")
    public ResponseEntity<OfferLetter> getOfferLetterById(@PathVariable Long id) {
        return ResponseEntity.ok(offerLetterService.getOfferLetterById(id));
    }

    /**
     * List all generated offer letters.
     */
    @GetMapping
    @Operation(summary = "List All Generated Offer Letters")
    public ResponseEntity<List<OfferLetterResponse>> getAllOfferLetters() {
        return ResponseEntity.ok(offerLetterService.getAllOfferLetters());
    }

    private String sanitizeFileName(String name) {
        if (name == null) return "Candidate";
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
