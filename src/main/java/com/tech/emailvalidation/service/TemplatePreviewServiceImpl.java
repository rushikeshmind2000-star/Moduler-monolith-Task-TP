package com.tech.emailvalidation.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitUntilState;
import com.tech.emailvalidation.dto.TemplatePreviewRequest;
import com.tech.emailvalidation.dto.TemplatePreviewResponse;
import com.tech.emailvalidation.exception.EmailValidationException;
import com.tech.emailvalidation.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TemplatePreviewServiceImpl implements TemplatePreviewService {

    private static final String PREVIEWS_DIR = "generated-previews";

    @Override
    public byte[] renderHtmlToImage(TemplatePreviewRequest request) {
        String cleanName = sanitizeTemplateName(request.getTemplateName());
        byte[] imageBytes = captureScreenshot(request.getHtml());

        // Save a copy to disk for persistence / download
        saveToDisk(cleanName + ".png", imageBytes);

        return imageBytes;
    }

    @Override
    public TemplatePreviewResponse generatePreviewMetadata(TemplatePreviewRequest request, String baseUrl) {
        String cleanName = sanitizeTemplateName(request.getTemplateName());
        String fileName = cleanName + ".png";
        byte[] imageBytes = captureScreenshot(request.getHtml());

        saveToDisk(fileName, imageBytes);

        String downloadUrl = baseUrl + "/api/templates/download/" + fileName;

        return TemplatePreviewResponse.builder()
                .templateName(request.getTemplateName())
                .fileName(fileName)
                .downloadUrl(downloadUrl)
                .sizeInBytes(imageBytes.length)
                .status("SUCCESS")
                .build();
    }

    @Override
    public byte[] getSavedPreviewImage(String fileName) {
        Path filePath = Paths.get(PREVIEWS_DIR, sanitizeFileName(fileName));
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Preview image not found: " + fileName);
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to read image file: {}", fileName, e);
            throw new EmailValidationException("Failed to read preview image file: " + e.getMessage());
        }
    }

    private byte[] captureScreenshot(String html) {
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            )) {
                NewContextOptions contextOptions = new NewContextOptions()
                        .setViewportSize(1024, 768)
                        .setDeviceScaleFactor(1.5); // high resolution crisp text

                var context = browser.newContext(contextOptions);
                Page page = context.newPage();

                // Load the HTML content
                page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.LOAD));

                // Take full-page PNG screenshot
                return page.screenshot(new Page.ScreenshotOptions()
                        .setFullPage(true)
                        .setType(ScreenshotType.PNG));
            }
        } catch (Exception e) {
            log.error("Error while rendering HTML to PNG using Playwright", e);
            throw new EmailValidationException("Failed to render HTML template to image: " + e.getMessage());
        }
    }

    private void saveToDisk(String fileName, byte[] content) {
        try {
            Path directory = Paths.get(PREVIEWS_DIR);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            Path destination = directory.resolve(fileName);
            Files.write(destination, content);
            log.info("Saved preview image to: {}", destination.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not save preview image to disk: {}", e.getMessage());
        }
    }

    private String sanitizeTemplateName(String name) {
        if (name == null || name.isBlank()) {
            return "template-" + System.currentTimeMillis();
        }
        String clean = name.replaceAll("[^a-zA-Z0-9-_]", "-").toLowerCase();
        if (clean.endsWith(".png")) {
            clean = clean.substring(0, clean.length() - 4);
        }
        return clean;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        // Protect against path traversal
        return Paths.get(fileName).getFileName().toString();
    }
}
