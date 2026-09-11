package com.factoryos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseOrderItemRequest(
        @NotNull(message = "Product ID cannot be null")
        Long productId,

        @NotNull(message = "Quantity cannot be null")
        @Min(value = 1, message = "Quantity must be greater than 0")
        Integer quantity,

        @NotNull(message = "Unit price cannot be null")
        @DecimalMin(value = "0.0", message = "Unit price must be greater than or equal to 0")
        BigDecimal unitPrice
) {
}

