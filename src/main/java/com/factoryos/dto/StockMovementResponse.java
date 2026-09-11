package com.factoryos.dto;

import com.factoryos.entity.StockMovementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Immutable audit log entry of an inventory stock mutation")
public record StockMovementResponse(
        @Schema(description = "Unique movement record ID", example = "101")
        Long id,

        @Schema(description = "Associated product ID", example = "1")
        Long productId,

        @Schema(description = "Type of stock transaction: STOCK_IN, STOCK_OUT, or ADJUSTMENT", example = "STOCK_IN")
        StockMovementType movementType,

        @Schema(description = "Quantity moved in transaction (positive integer)", example = "25")
        Integer quantity,

        @Schema(description = "Operational reference or external tracking document ID", example = "GRN-1001")
        String reference,

        @Schema(description = "Audit explanation or business reasoning", example = "Initial supplier delivery")
        String reason,

        @Schema(description = "UTC timestamp when the movement was committed", example = "2026-09-11T09:45:00Z")
        Instant createdAt
) {}
