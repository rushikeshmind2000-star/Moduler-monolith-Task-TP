package com.tech.emailvalidation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    private String productName;

    @PositiveOrZero(message = "Amount must be zero or positive")
    private Double amount;

    private Long customerId;
}
