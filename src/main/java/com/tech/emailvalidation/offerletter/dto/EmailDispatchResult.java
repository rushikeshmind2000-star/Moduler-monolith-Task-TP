package com.tech.emailvalidation.offerletter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDispatchResult {

    private boolean success;
    private String statusMessage;
    private String errorDetails;

    public static EmailDispatchResult ok(String message) {
        return EmailDispatchResult.builder()
                .success(true)
                .statusMessage(message)
                .build();
    }

    public static EmailDispatchResult failed(String message, String errorDetails) {
        return EmailDispatchResult.builder()
                .success(false)
                .statusMessage(message)
                .errorDetails(errorDetails)
                .build();
    }
}
