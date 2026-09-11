package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Product details and operational metadata")
public record ProductResponse(
        @Schema(description = "Unique database product ID", example = "1")
        Long id,

        @Schema(description = "Stock Keeping Unit (SKU) identifier", example = "MOTOR-001")
        String sku,

        @Schema(description = "Product display name", example = "Industrial Electric Motor")
        String name,

        @Schema(description = "Detailed product description", example = "5 HP 3-phase heavy-duty electric induction motor")
        String description,

        @Schema(description = "Product category", example = "Motors")
        String category,

        @Schema(description = "Current unit selling/catalog price", example = "18500.00")
        BigDecimal unitPrice,

        @Schema(description = "Stock reorder threshold level", example = "10")
        Integer reorderLevel,

        @Schema(description = "Active status flag (inactive products cannot be stocked or ordered)", example = "true")
        Boolean active,

        @Schema(description = "UTC creation timestamp", example = "2026-09-11T09:30:00Z")
        Instant createdAt,

        @Schema(description = "UTC last update timestamp", example = "2026-09-11T10:00:00Z")
        Instant updatedAt
) {}
