package com.tech.emailvalidation.MODULE_API.GlobalException;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
