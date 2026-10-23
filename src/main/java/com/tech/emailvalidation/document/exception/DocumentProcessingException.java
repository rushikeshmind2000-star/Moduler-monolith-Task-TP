package com.tech.emailvalidation.document.exception;

/**
 * Exception thrown when Word document processing fails
 * (e.g., reading, writing, or replacing placeholders).
 */
public class DocumentProcessingException extends RuntimeException {

    public DocumentProcessingException(String message) {
        super(message);
    }

    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
