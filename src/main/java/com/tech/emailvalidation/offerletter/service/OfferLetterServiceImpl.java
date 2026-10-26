package com.tech.emailvalidation.offerletter.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tech.emailvalidation.exception.ResourceNotFoundException;
import com.tech.emailvalidation.offerletter.config.OfferLetterProperties;
import com.tech.emailvalidation.offerletter.dto.EmailDispatchResult;
import com.tech.emailvalidation.offerletter.dto.OfferLetterRequest;
import com.tech.emailvalidation.offerletter.dto.OfferLetterResponse;
import com.tech.emailvalidation.offerletter.entity.OfferLetter;
import com.tech.emailvalidation.offerletter.repository.OfferLetterRepository;

@Service
public class OfferLetterServiceImpl implements OfferLetterService {

    private static final Logger log = LoggerFactory.getLogger(OfferLetterServiceImpl.class);

    private final PdfGenerationService pdfGenerationService;
    private final EmailSenderService emailSenderService;
    private final OfferLetterRepository offerLetterRepository;
    private final OfferLetterProperties properties;

    public OfferLetterServiceImpl(
            PdfGenerationService pdfGenerationService,
            EmailSenderService emailSenderService,
            OfferLetterRepository offerLetterRepository,
            OfferLetterProperties properties) {
        this.pdfGenerationService = pdfGenerationService;
        this.emailSenderService = emailSenderService;
        this.offerLetterRepository = offerLetterRepository;
        this.properties = properties;
    }

    @Override
    @Transactional
    public OfferLetterResponse generateAndSendOfferLetter(OfferLetterRequest request) {
        log.info("Processing offer letter generation request for: {} ({})", request.getEmployeeName(), request.getEmail());

        // Resolve default values if not provided
        String companyName = resolveCompanyName(request);
        String designation = resolveDesignation(request);
        String department = resolveDepartment(request);
        String joiningDate = resolveJoiningDate(request);

        String referenceNumber = generateReferenceNumber();
        String pdfFileName = "Offer_Letter_" + sanitizeFileName(request.getEmployeeName()) + ".pdf";

        // 1. Generate Offer Letter PDF
        byte[] pdfBytes = pdfGenerationService.generateOfferLetterPdf(request, referenceNumber);

        // 2. Dispatch Email if requested
        boolean emailSent = false;
        String emailStatusMessage = "Email dispatch disabled by request parameter";

        if (Boolean.TRUE.equals(request.getSendEmail())) {
            EmailDispatchResult dispatchResult = emailSenderService.sendOfferLetterEmail(
                    request.getEmail().trim(),
                    request.getEmployeeName().trim(),
                    designation,
                    department,
                    request.getPackageAmount().trim(),
                    joiningDate,
                    request.getAddress().trim(),
                    companyName,
                    pdfBytes,
                    pdfFileName
            );
            emailSent = dispatchResult.isSuccess();
            emailStatusMessage = dispatchResult.isSuccess()
                    ? dispatchResult.getStatusMessage()
                    : dispatchResult.getStatusMessage() + " (" + dispatchResult.getErrorDetails() + ")";
        }

        // 3. Persist Offer Letter entity to database
        OfferLetter entity = OfferLetter.builder()
                .referenceNumber(referenceNumber)
                .employeeName(request.getEmployeeName().trim())
                .email(request.getEmail().trim())
                .address(request.getAddress().trim())
                .packageAmount(request.getPackageAmount().trim())
                .designation(designation)
                .department(department)
                .joiningDate(joiningDate)
                .companyName(companyName)
                .emailSent(emailSent)
                .emailStatus(emailStatusMessage)
                .pdfData(pdfBytes)
                .createdAt(LocalDateTime.now())
                .build();

        OfferLetter savedEntity = offerLetterRepository.save(entity);

        // 4. Build response
        String overallMessage = emailSent
                ? "Offer letter generated successfully and dispatched to " + request.getEmail()
                : "Offer letter generated successfully. Note: " + emailStatusMessage;

        return mapToResponse(savedEntity, overallMessage);
    }

    @Override
    public byte[] generatePdfOnly(OfferLetterRequest request) {
        String referenceNumber = generateReferenceNumber();
        return pdfGenerationService.generateOfferLetterPdf(request, referenceNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public OfferLetter getOfferLetterById(Long id) {
        return offerLetterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer letter not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getOfferLetterPdfById(Long id) {
        OfferLetter offerLetter = getOfferLetterById(id);
        if (offerLetter.getPdfData() == null || offerLetter.getPdfData().length == 0) {
            throw new ResourceNotFoundException("PDF data is empty or not found for offer letter ID: " + id);
        }
        return offerLetter.getPdfData();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferLetterResponse> getAllOfferLetters() {
        return offerLetterRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(entity -> mapToResponse(entity, "Retrieved successfully"))
                .collect(Collectors.toList());
    }

    private OfferLetterResponse mapToResponse(OfferLetter entity, String message) {
        return OfferLetterResponse.builder()
                .id(entity.getId())
                .referenceNumber(entity.getReferenceNumber())
                .employeeName(entity.getEmployeeName())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .packageAmount(entity.getPackageAmount())
                .designation(entity.getDesignation())
                .department(entity.getDepartment())
                .joiningDate(entity.getJoiningDate())
                .companyName(entity.getCompanyName())
                .status("SUCCESS")
                .message(message)
                .emailSent(entity.getEmailSent())
                .emailDeliveryStatus(entity.getEmailStatus())
                .generatedAt(entity.getCreatedAt())
                .pdfDownloadUrl("/api/offer-letter/" + entity.getId() + "/download")
                .build();
    }

    private String generateReferenceNumber() {
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        int year = LocalDate.now().getYear();
        return "TP-OFFER-" + year + "-" + random;
    }

    private String sanitizeFileName(String name) {
        if (name == null) return "Candidate";
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private String resolveCompanyName(OfferLetterRequest req) {
        if (req.getCompanyName() != null && !req.getCompanyName().isBlank()) {
            return req.getCompanyName().trim();
        }
        return properties.getCompany().getName();
    }

    private String resolveDesignation(OfferLetterRequest req) {
        if (req.getDesignation() != null && !req.getDesignation().isBlank()) {
            return req.getDesignation().trim();
        }
        return "Software Engineer";
    }

    private String resolveDepartment(OfferLetterRequest req) {
        if (req.getDepartment() != null && !req.getDepartment().isBlank()) {
            return req.getDepartment().trim();
        }
        return "Engineering";
    }

    private String resolveJoiningDate(OfferLetterRequest req) {
        if (req.getJoiningDate() != null && !req.getJoiningDate().isBlank()) {
            return req.getJoiningDate().trim();
        }
        return LocalDate.now().plusDays(15).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
    }
}
