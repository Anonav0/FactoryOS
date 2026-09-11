package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for reconciling physical warehouse stock to a new exact count")
public record StockAdjustmentRequest(
        @Schema(description = "Target product ID", example = "1")
        @NotNull(message = "Product ID cannot be null")
        Long productId,

        @Schema(description = "Reconciled physical quantity count (zero or positive)", example = "18")
        @NotNull(message = "New quantity cannot be null")
        @PositiveOrZero(message = "New quantity must be greater than or equal to 0")
        Integer newQuantity,

        @Schema(description = "Audit sheet or inventory recount reference", example = "AUDIT-2026-Q3")
        @Size(max = 100, message = "Reference must not exceed 100 characters")
        String reference,

        @Schema(description = "Explanation for discrepancy or adjustment reason", example = "Quarterly physical stock reconciliation")
        @Size(max = 255, message = "Reason must not exceed 255 characters")
        String reason
) {}
