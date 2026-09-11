package com.factoryos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequest(
        @NotNull(message = "Product ID cannot be null")
        Long productId,

        @NotNull(message = "New quantity cannot be null")
        @PositiveOrZero(message = "New quantity must be greater than or equal to 0")
        Integer newQuantity,

        @Size(max = 100, message = "Reference must not exceed 100 characters")
        String reference,

        @Size(max = 255, message = "Reason must not exceed 255 characters")
        String reason
) {}

