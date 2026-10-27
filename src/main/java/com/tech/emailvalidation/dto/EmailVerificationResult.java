package com.tech.emailvalidation.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationResult {
    private String email;
    private String domain;
    private boolean valid;
    private boolean formatValid;
    private boolean mxValid;
    private boolean disposable;
    private List<String> mxRecords;
    private String primaryMx;
    private String failureReason;
}
