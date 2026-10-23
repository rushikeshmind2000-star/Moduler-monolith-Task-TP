package com.tech.emailvalidation.document.service;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.tech.emailvalidation.document.exception.GoogleDriveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service responsible for all Google Drive interactions.
 *
 * <h3>Google Docs vs .docx handling</h3>
 * The master document may be a <b>Google Docs native</b> file (created in
 * Google Docs) rather than an uploaded {@code .docx}. The difference matters:
 * <ul>
 *   <li><b>Download:</b> Native Google Docs files cannot be downloaded directly;
 *       they must be <em>exported</em> to {@code .docx} format using
 *       {@code files().export()}.</li>
 *   <li><b>Upload:</b> When updating a native Google Docs file with {@code .docx}
 *       content, Google Drive automatically converts it back to Google Docs
 *       format.</li>
 * </ul>
 *
 * <h3>Key design decisions</h3>
 * <ul>
 *   <li>The file ID is externalized to {@code application.yml} so it can be
 *       changed without recompilation.</li>
 *   <li>All I/O exceptions are wrapped in {@link GoogleDriveException} so the
 *       controller layer never leaks Google-specific details.</li>
 *   <li>This service is intentionally <em>not</em> {@code synchronized} —
 *       concurrency control is handled at the controller level.</li>
 * </ul>
 */
@Service
public class GoogleDriveService {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveService.class);

    private static final String DOCX_MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final Drive driveService;
    private final String fileId;

    public GoogleDriveService(Drive driveService,
                              @Value("${google.drive.file-id}") String fileId) {
        this.driveService = driveService;
        this.fileId = fileId;
    }

    /**
     * Downloads the master document from Google Drive.
     *
     * First checks the file's MIME type:
     * <ul>
     *   <li>If it's a Google Docs native file → exports as .docx</li>
     *   <li>If it's an uploaded .docx file → downloads directly</li>
     * </ul>
     *
     * @return raw bytes of the .docx file
     * @throws GoogleDriveException if the download fails
     */
    public byte[] downloadDocument() {
        try {
            log.info("Downloading document from Google Drive, fileId={}", fileId);

            // Check the file's MIME type to determine download method
            File fileMeta = driveService.files().get(fileId)
                    .setFields("mimeType, name")
                    .execute();

            String mimeType = fileMeta.getMimeType();
            log.info("File '{}' has MIME type: {}", fileMeta.getName(), mimeType);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            if ("application/vnd.google-apps.document".equals(mimeType)) {
                // Google Docs native file → export as .docx
                log.info("Exporting Google Docs file as .docx");
                driveService.files().export(fileId, DOCX_MIME_TYPE)
                        .executeMediaAndDownloadTo(outputStream);
            } else {
                // Uploaded .docx or other binary file → direct download
                log.info("Downloading binary file directly");
                driveService.files().get(fileId)
                        .executeMediaAndDownloadTo(outputStream);
            }

            byte[] content = outputStream.toByteArray();
            log.info("Downloaded {} bytes from Google Drive", content.length);
            return content;

        } catch (IOException e) {
            throw new GoogleDriveException(
                    "Failed to download document from Google Drive (fileId=" + fileId + ")", e);
        }
    }

    /**
     * Uploads the updated document bytes back to the <em>same</em> Google Drive file.
     * Uses {@code Drive.Files.Update} so no new file is created.
     *
     * For Google Docs native files, Google Drive automatically converts the
     * uploaded .docx content back to Google Docs format.
     *
     * @param updatedContent the updated .docx bytes
     * @throws GoogleDriveException if the upload fails
     */
    public void updateDocument(byte[] updatedContent) {
        try {
            log.info("Updating document in Google Drive, fileId={}, size={} bytes",
                    fileId, updatedContent.length);

            File fileMetadata = new File();
            ByteArrayContent mediaContent =
                    new ByteArrayContent(DOCX_MIME_TYPE, updatedContent);

            driveService.files().update(fileId, fileMetadata, mediaContent)
                    .execute();

            log.info("Successfully updated document in Google Drive");

        } catch (IOException e) {
            throw new GoogleDriveException(
                    "Failed to update document in Google Drive (fileId=" + fileId + ")", e);
        }
    }

    /**
     * Returns the configured Google Drive file ID.
     */
    public String getFileId() {
        return fileId;
    }
}
