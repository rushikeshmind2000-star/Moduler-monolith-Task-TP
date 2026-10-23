package com.tech.emailvalidation.controller;

import com.tech.emailvalidation.dto.ApiResponse;
import com.tech.emailvalidation.service.EmailValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/validation")
@Tag(name = "Email Validation", description = "Endpoints for verifying email address validity")
public class EmailValidationController {

    private final EmailValidationService emailValidationService;

    public EmailValidationController(EmailValidationService emailValidationService) {
        this.emailValidationService = emailValidationService;
    }

    @GetMapping("/email")
    @Operation(summary = "Validate Email Address")
    public ApiResponse validateEmail(@RequestParam("email") String email) {
        return emailValidationService.validateEmail(email);
    }
}