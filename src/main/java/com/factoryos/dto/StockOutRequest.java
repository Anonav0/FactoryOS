package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for consuming or dispatching stock from inventory")
public record StockOutRequest(
        @Schema(description = "Target product ID", example = "1")
        @NotNull(message = "Product ID cannot be null")
        Long productId,

        @Schema(description = "Positive quantity of units to deduct (must not exceed quantityAvailable)", example = "5")
        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Stock-out quantity must be greater than 0")
        Integer quantity,

        @Schema(description = "Work order or production batch reference code", example = "PROD-2001")
        @Size(max = 100, message = "Reference must not exceed 100 characters")
        String reference,

        @Schema(description = "Business justification or usage note", example = "Production assembly line consumption")
        @Size(max = 255, message = "Reason must not exceed 255 characters")
        String reason
) {}
