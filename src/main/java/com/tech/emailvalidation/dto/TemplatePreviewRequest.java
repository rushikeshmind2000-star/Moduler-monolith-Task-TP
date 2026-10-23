package com.tech.emailvalidation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplatePreviewRequest {

    @NotBlank(message = "templateName is required")
    private String templateName;

    @NotBlank(message = "html is required")
    private String html;
}
