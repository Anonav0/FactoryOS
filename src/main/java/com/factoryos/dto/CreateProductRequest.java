package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request payload for creating a new industrial product")
public record CreateProductRequest(
        @Schema(description = "Unique Stock Keeping Unit (SKU) code", example = "MOTOR-001")
        @NotBlank(message = "SKU cannot be blank")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        String sku,

        @Schema(description = "Product display name", example = "Industrial Electric Motor")
        @NotBlank(message = "Product name cannot be blank")
        @Size(max = 150, message = "Product name must not exceed 150 characters")
        String name,

        @Schema(description = "Detailed product specifications or usage notes", example = "5 HP 3-phase heavy-duty electric induction motor")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Product category", example = "Motors")
        @NotBlank(message = "Category cannot be blank")
        @Size(max = 100, message = "Category must not exceed 100 characters")
        String category,

        @Schema(description = "Unit price in standard currency", example = "18500.00")
        @NotNull(message = "Unit price cannot be null")
        @PositiveOrZero(message = "Unit price must be greater than or equal to 0")
        BigDecimal unitPrice,

        @Schema(description = "Minimum stock threshold triggering reorder alert", example = "10")
        @NotNull(message = "Reorder level cannot be null")
        @PositiveOrZero(message = "Reorder level must be greater than or equal to 0")
        Integer reorderLevel,

        @Schema(description = "Active status flag (defaults to true if null)", example = "true")
        Boolean active
) {}
