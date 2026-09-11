package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for adding physical stock to inventory")
public record StockInRequest(
        @Schema(description = "Target product ID", example = "1")
        @NotNull(message = "Product ID cannot be null")
        Long productId,

        @Schema(description = "Positive quantity of units to add", example = "25")
        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Stock-in quantity must be greater than 0")
        Integer quantity,

        @Schema(description = "External document or delivery reference code", example = "GRN-1001")
        @Size(max = 100, message = "Reference must not exceed 100 characters")
        String reference,

        @Schema(description = "Business justification or operational reason", example = "Supplier delivery received")
        @Size(max = 255, message = "Reason must not exceed 255 characters")
        String reason
) {}
