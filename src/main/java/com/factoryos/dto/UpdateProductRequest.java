package com.factoryos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank(message = "Product name cannot be blank")
        @Size(max = 150, message = "Product name must not exceed 150 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotBlank(message = "Category cannot be blank")
        @Size(max = 100, message = "Category must not exceed 100 characters")
        String category,

        @NotNull(message = "Unit price cannot be null")
        @PositiveOrZero(message = "Unit price must be greater than or equal to 0")
        BigDecimal unitPrice,

        @NotNull(message = "Reorder level cannot be null")
        @PositiveOrZero(message = "Reorder level must be greater than or equal to 0")
        Integer reorderLevel,

        Boolean active
) {}

