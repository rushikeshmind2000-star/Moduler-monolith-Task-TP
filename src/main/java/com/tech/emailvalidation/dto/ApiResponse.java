package com.tech.emailvalidation.dto;

public class ApiResponse {

    private VerifyResult verifyResult;
    private String message;
    private boolean valid;

    public ApiResponse() {
    }

    public ApiResponse(VerifyResult verifyResult, String message, boolean valid) {
        this.verifyResult = verifyResult;
        this.message = message;
        this.valid = valid;
    }

    public VerifyResult getVerifyResult() {
        return verifyResult;
    }

    public void setVerifyResult(VerifyResult verifyResult) {
        this.verifyResult = verifyResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}