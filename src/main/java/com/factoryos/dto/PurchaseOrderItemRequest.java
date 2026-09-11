package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Line item inside a Purchase Order creation request")
public record PurchaseOrderItemRequest(
        @Schema(description = "Active product ID to purchase", example = "1")
        @NotNull(message = "Product ID cannot be null")
        Long productId,

        @Schema(description = "Ordered quantity in units (must be >= 1)", example = "10")
        @NotNull(message = "Quantity cannot be null")
        @Min(value = 1, message = "Quantity must be greater than 0")
        Integer quantity,

        @Schema(description = "Agreed purchase unit price in standard currency", example = "18500.00")
        @NotNull(message = "Unit price cannot be null")
        @DecimalMin(value = "0.0", message = "Unit price must be greater than or equal to 0")
        BigDecimal unitPrice
) {
}
