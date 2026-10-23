package com.tech.emailvalidation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplatePreviewResponse {

    private String templateName;
    private String fileName;
    private String downloadUrl;
    private long sizeInBytes;
    private String status;
}
