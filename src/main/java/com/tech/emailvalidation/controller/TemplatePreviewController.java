package com.tech.emailvalidation.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tech.emailvalidation.dto.TemplatePreviewRequest;
import com.tech.emailvalidation.dto.TemplatePreviewResponse;
import com.tech.emailvalidation.service.TemplatePreviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/templates")
@Tag(name = "Template Preview", description = "APIs for rendering HTML templates into PNG images using Playwright")
public class TemplatePreviewController {

    private final TemplatePreviewService templatePreviewService;

    public TemplatePreviewController(TemplatePreviewService templatePreviewService) {
        this.templatePreviewService = templatePreviewService;
    }

    /**
     * POST /api/templates/preview
     * Directly returns the generated PNG image as a binary response.
     * In Postman, this automatically displays the visual image preview.
     */
    @PostMapping(value = "/preview", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Render HTML to PNG preview", description = "Converts HTML to a PNG image returned directly as an image file")
    public ResponseEntity<byte[]> previewTemplate(@Valid @RequestBody TemplatePreviewRequest request) {
        byte[] imageBytes = templatePreviewService.renderHtmlToImage(request);
        String fileName = request.getTemplateName() + ".png";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDisposition(ContentDisposition.inline().filename(fileName).build());
        headers.set("X-Preview-Filename", fileName);
        headers.set("X-Download-Url", "/api/templates/download/" + fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(imageBytes);
    }

    /**
     * POST /api/templates/preview/json
     * Generates the image, stores it, and returns JSON containing the download URL.
     */
    @PostMapping(value = "/preview/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Render HTML to PNG and return download URL JSON")
    public ResponseEntity<TemplatePreviewResponse> previewTemplateJson(
            @Valid @RequestBody TemplatePreviewRequest request,
            HttpServletRequest httpRequest) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        TemplatePreviewResponse response = templatePreviewService.generatePreviewMetadata(request, baseUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/templates/download/{fileName}
     * Download or view the generated PNG image file by name.
     */
    @GetMapping(value = "/download/{fileName}", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Download generated PNG image")
    public ResponseEntity<byte[]> downloadPreview(@PathVariable String fileName) {
        byte[] imageBytes = templatePreviewService.getSavedPreviewImage(fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(imageBytes);
    }
}
