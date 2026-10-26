package com.tech.emailvalidation.offerletter.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after generating an Offer Letter")
public class OfferLetterResponse {

    @Schema(description = "Unique database ID of the generated offer letter", example = "1")
    private Long id;

    @Schema(description = "Generated reference number", example = "TP-OFFER-2026-1048")
    private String referenceNumber;

    @Schema(description = "Candidate employee name", example = "Rushikesh Shinde")
    private String employeeName;

    @Schema(description = "Recipient email address", example = "candidate@example.com")
    private String email;

    @Schema(description = "Candidate address", example = "Flat 402, Skyline Towers, Hinjewadi, Pune 411057")
    private String address;

    @Schema(description = "Annual package", example = "12 LPA")
    private String packageAmount;

    @Schema(description = "Offered designation", example = "Senior Java Developer")
    private String designation;

    @Schema(description = "Department", example = "Product Engineering")
    private String department;

    @Schema(description = "Joining date", example = "2026-11-15")
    private String joiningDate;

    @Schema(description = "Company name", example = "TechPlus Technologies Pvt. Ltd.")
    private String companyName;

    @Schema(description = "Status of the operation", example = "SUCCESS")
    private String status;

    @Schema(description = "Descriptive message about generation and email status", example = "Offer letter generated successfully and sent to candidate@example.com")
    private String message;

    @Schema(description = "Flag indicating whether email was successfully dispatched", example = "true")
    private Boolean emailSent;

    @Schema(description = "Email delivery detail or failure note", example = "Email delivered with attached PDF")
    private String emailDeliveryStatus;

    @Schema(description = "Timestamp when the offer letter was generated")
    private LocalDateTime generatedAt;

    @Schema(description = "Direct URL endpoint to download the generated PDF", example = "/api/offer-letter/1/download")
    private String pdfDownloadUrl;
}
