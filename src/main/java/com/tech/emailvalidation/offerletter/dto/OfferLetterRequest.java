package com.tech.emailvalidation.offerletter.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for generating and sending an Offer Letter")
public class OfferLetterRequest {

    @NotBlank(message = "Employee name is required")
    @JsonAlias({"employee_name", "name"})
    @JsonProperty("employeeName")
    @Schema(description = "Full name of the candidate / employee", example = "Rushikesh Shinde", requiredMode = Schema.RequiredMode.REQUIRED)
    private String employeeName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @JsonAlias({"mail", "employee_mail", "employeeEmail"})
    @JsonProperty("email")
    @Schema(description = "Candidate email address where offer letter PDF will be delivered", example = "candidate@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Address is required")
    @JsonAlias({"employee_address", "residence_address"})
    @JsonProperty("address")
    @Schema(description = "Candidate residential or communication address", example = "Flat 402, Skyline Towers, Hinjewadi, Pune 411057", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @NotBlank(message = "Package is required")
    @JsonAlias({"package", "salary", "salaryPackage", "ctc"})
    @JsonProperty("packageAmount")
    @Schema(description = "Annual compensation package (CTC)", example = "12 LPA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String packageAmount;

    @JsonAlias({"role", "job_title", "position"})
    @Schema(description = "Job title / Designation offered", example = "Senior Java Developer", defaultValue = "Software Engineer")
    private String designation;

    @JsonAlias({"dept"})
    @Schema(description = "Department name", example = "Product Engineering", defaultValue = "Information Technology")
    private String department;

    @JsonAlias({"joining_date", "doj"})
    @Schema(description = "Expected date of joining (e.g. YYYY-MM-DD or readable text)", example = "2026-11-15")
    private String joiningDate;

    @JsonAlias({"company"})
    @Schema(description = "Company name (optional, defaults to configured company)", example = "TechPlus Technologies Pvt. Ltd.")
    private String companyName;

    @JsonAlias({"reporting_manager", "manager"})
    @Schema(description = "Reporting manager name / role", example = "Director of Engineering")
    private String reportingManager;

    @Builder.Default
    @JsonProperty("sendEmail")
    @Schema(description = "Whether to immediately dispatch congratulatory email with attached PDF", defaultValue = "true")
    private Boolean sendEmail = true;

    @Builder.Default
    @JsonProperty("asyncEmail")
    @Schema(description = "Whether to dispatch email asynchronously in background for sub-second API response time", defaultValue = "true")
    private Boolean asyncEmail = true;
}
