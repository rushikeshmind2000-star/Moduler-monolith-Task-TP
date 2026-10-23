package com.tech.emailvalidation.dto;

public class VerifyResult {

    private String email;
    private boolean syntaxValid;
    private boolean hasMx;
    private boolean smtpAcceptsRcpt;
    private String mxTried;
    private Integer smtpCode;
    private String smtpMessage;

    public VerifyResult() {
    }

    public VerifyResult(String email, boolean syntaxValid, boolean hasMx, boolean smtpAcceptsRcpt, String mxTried, Integer smtpCode, String smtpMessage) {
        this.email = email;
        this.syntaxValid = syntaxValid;
        this.hasMx = hasMx;
        this.smtpAcceptsRcpt = smtpAcceptsRcpt;
        this.mxTried = mxTried;
        this.smtpCode = smtpCode;
        this.smtpMessage = smtpMessage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSyntaxValid() {
        return syntaxValid;
    }

    public void setSyntaxValid(boolean syntaxValid) {
        this.syntaxValid = syntaxValid;
    }

    public boolean isHasMx() {
        return hasMx;
    }

    public void setHasMx(boolean hasMx) {
        this.hasMx = hasMx;
    }

    public boolean isSmtpAcceptsRcpt() {
        return smtpAcceptsRcpt;
    }

    public void setSmtpAcceptsRcpt(boolean smtpAcceptsRcpt) {
        this.smtpAcceptsRcpt = smtpAcceptsRcpt;
    }

    public String getMxTried() {
        return mxTried;
    }

    public void setMxTried(String mxTried) {
        this.mxTried = mxTried;
    }

    public Integer getSmtpCode() {
        return smtpCode;
    }

    public void setSmtpCode(Integer smtpCode) {
        this.smtpCode = smtpCode;
    }

    public String getSmtpMessage() {
        return smtpMessage;
    }

    public void setSmtpMessage(String smtpMessage) {
        this.smtpMessage = smtpMessage;
    }
}