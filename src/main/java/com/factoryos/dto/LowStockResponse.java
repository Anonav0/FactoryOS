package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary of product with inventory at or below minimum reorder threshold")
public record LowStockResponse(
        @Schema(description = "Product ID", example = "5")
        Long productId,

        @Schema(description = "Product SKU code", example = "PUMP-001")
        String sku,

        @Schema(description = "Product display name", example = "Hydraulic Gear Pump")
        String productName,

        @Schema(description = "Current available quantity", example = "3")
        Integer quantityAvailable,

        @Schema(description = "Reorder threshold level configured for product", example = "5")
        Integer reorderLevel,

        @Schema(description = "Always true for items returned by low-stock query", example = "true")
        Boolean lowStock
) {}
