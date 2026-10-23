package com.tech.emailvalidation.service;

import com.tech.emailvalidation.dto.TemplatePreviewRequest;
import com.tech.emailvalidation.dto.TemplatePreviewResponse;

public interface TemplatePreviewService {

    byte[] renderHtmlToImage(TemplatePreviewRequest request);

    TemplatePreviewResponse generatePreviewMetadata(TemplatePreviewRequest request, String baseUrl);

    byte[] getSavedPreviewImage(String fileName);
}
