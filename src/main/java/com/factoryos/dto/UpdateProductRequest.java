package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request payload for updating an existing product's details")
public record UpdateProductRequest(
        @Schema(description = "Updated product display name", example = "Industrial Electric Motor V2")
        @NotBlank(message = "Product name cannot be blank")
        @Size(max = 150, message = "Product name must not exceed 150 characters")
        String name,

        @Schema(description = "Updated product specifications", example = "5 HP 3-phase heavy-duty motor with thermal protection")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Updated category", example = "Motors")
        @NotBlank(message = "Category cannot be blank")
        @Size(max = 100, message = "Category must not exceed 100 characters")
        String category,

        @Schema(description = "Updated unit price", example = "19200.00")
        @NotNull(message = "Unit price cannot be null")
        @PositiveOrZero(message = "Unit price must be greater than or equal to 0")
        BigDecimal unitPrice,

        @Schema(description = "Updated reorder threshold level", example = "12")
        @NotNull(message = "Reorder level cannot be null")
        @PositiveOrZero(message = "Reorder level must be greater than or equal to 0")
        Integer reorderLevel,

        @Schema(description = "Active status flag", example = "true")
        Boolean active
) {}
