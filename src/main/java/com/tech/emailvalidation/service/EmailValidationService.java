package com.tech.emailvalidation.service;

import com.tech.emailvalidation.dto.ApiResponse;

public interface EmailValidationService {
    ApiResponse validateEmail(String email);
}