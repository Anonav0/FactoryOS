package com.factoryos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockOutRequest(
        @NotNull(message = "Product ID cannot be null")
        Long productId,

        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Stock-out quantity must be greater than 0")
        Integer quantity,

        @Size(max = 100, message = "Reference must not exceed 100 characters")
        String reference,

        @Size(max = 255, message = "Reason must not exceed 255 characters")
        String reason
) {}

