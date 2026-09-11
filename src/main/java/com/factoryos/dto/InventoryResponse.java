package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Real-time inventory levels and status for a product")
public record InventoryResponse(
        @Schema(description = "Unique inventory record ID", example = "1")
        Long id,

        @Schema(description = "Associated product ID", example = "1")
        Long productId,

        @Schema(description = "Product SKU code", example = "MOTOR-001")
        String sku,

        @Schema(description = "Product display name", example = "Industrial Electric Motor")
        String productName,

        @Schema(description = "Current available physical stock on hand", example = "45")
        Integer quantityAvailable,

        @Schema(description = "Quantity allocated or reserved for pending orders", example = "0")
        Integer reservedQuantity,

        @Schema(description = "Flag indicating if available stock is at or below the reorder threshold", example = "false")
        Boolean lowStock,

        @Schema(description = "UTC timestamp of latest inventory movement", example = "2026-09-11T10:30:00Z")
        Instant lastUpdated
) {}
