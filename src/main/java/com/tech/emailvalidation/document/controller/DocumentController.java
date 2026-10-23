package com.tech.emailvalidation.document.controller;

import com.tech.emailvalidation.document.dto.CustomerRequest;
import com.tech.emailvalidation.document.service.DocumentGenerationService;
import com.tech.emailvalidation.document.service.GoogleDriveService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the customer document update-and-download workflow.
 *
 * <h3>Endpoint</h3>
 * {@code POST /api/documents/update-and-download}
 *
 * <h3>Request flow</h3>
 * <ol>
 *   <li>Receive and validate customer JSON via {@link CustomerRequest}.</li>
 *   <li>Acquire a lock (only one request processes at a time).</li>
 *   <li>Download the master .docx template from Google Drive.</li>
 *   <li>Replace all placeholders with the customer's data.</li>
 *   <li>Upload the updated .docx back to the <em>same</em> Google Drive file.</li>
 *   <li>Return the updated .docx bytes to the caller as a downloadable attachment.</li>
 * </ol>
 *
 * <h3>Concurrency</h3>
 * A {@code synchronized} block ensures that only one request is processed at a
 * time. This prevents race conditions where two concurrent requests could both
 * download the template, replace placeholders, and then overwrite each other's
 * changes in Google Drive.
 *
 * <p><b>Limitation:</b> This only works when the application is running as a
 * single instance. In a multi-instance deployment (e.g., behind a load
 * balancer), you would need distributed locking (e.g., Redis-based or
 * database-based locking) to prevent concurrent access across instances.</p>
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private static final String DOWNLOAD_FILENAME = "CustomerDetails.docx";

    /**
     * Lock object for synchronizing access to the shared Google Drive document.
     */
    private final Object documentLock = new Object();

    private final GoogleDriveService googleDriveService;
    private final DocumentGenerationService documentGenerationService;

    public DocumentController(GoogleDriveService googleDriveService,
                              DocumentGenerationService documentGenerationService) {
        this.googleDriveService = googleDriveService;
        this.documentGenerationService = documentGenerationService;
    }

    /**
     * Accepts customer details, updates the master Word document in Google
     * Drive, and returns the updated file for immediate download.
     *
     * @param request validated customer details
     * @return the updated .docx file as a downloadable response
     */
    @PostMapping("/update-and-download")
    public ResponseEntity<byte[]> updateAndDownload(@Valid @RequestBody CustomerRequest request) {

        log.info("Received update-and-download request for customer: {}", request.getCustomerName());

        byte[] updatedDocument;

        /*
         * Synchronize the entire download-modify-upload cycle so that
         * concurrent requests do not overwrite each other's changes.
         *
         * Inside the lock:
         *   1. Download the current template from Google Drive.
         *   2. Replace placeholders with the submitted customer data.
         *   3. Upload the modified document back to Google Drive.
         *
         * The lock is released only after the Drive upload completes,
         * ensuring the next request will see the latest version.
         */
        synchronized (documentLock) {
            // Step 1: Download the existing Word document from Google Drive
            byte[] templateBytes = googleDriveService.downloadDocument();
            log.info("Template downloaded, {} bytes", templateBytes.length);

            // Step 2: Replace placeholders with customer details
            updatedDocument = documentGenerationService.generateDocument(templateBytes, request);
            log.info("Document generated, {} bytes", updatedDocument.length);

            // Step 3: Upload the updated document back to Google Drive
            googleDriveService.updateDocument(updatedDocument);
            log.info("Document updated in Google Drive");
        }

        // Step 4: Return the updated document as a downloadable file
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(DOCX_CONTENT_TYPE));
        headers.setContentDispositionFormData("attachment", DOWNLOAD_FILENAME);
        headers.setContentLength(updatedDocument.length);

        log.info("Returning document for download: {} ({} bytes)",
                DOWNLOAD_FILENAME, updatedDocument.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(updatedDocument);
    }
}
