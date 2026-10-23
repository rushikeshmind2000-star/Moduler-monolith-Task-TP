package com.tech.emailvalidation.document.exception;

/**
 * Exception thrown when Google Drive operations fail
 * (e.g., authentication, download, or upload failures).
 */
public class GoogleDriveException extends RuntimeException {

    public GoogleDriveException(String message) {
        super(message);
    }

    public GoogleDriveException(String message, Throwable cause) {
        super(message, cause);
    }
}
