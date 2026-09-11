package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Line item detail within a Purchase Order response")
public record PurchaseOrderItemResponse(
        @Schema(description = "Unique item ID", example = "10")
        Long id,

        @Schema(description = "Purchased product ID", example = "1")
        Long productId,

        @Schema(description = "Product SKU", example = "MOTOR-001")
        String sku,

        @Schema(description = "Product name", example = "Industrial Electric Motor")
        String productName,

        @Schema(description = "Ordered quantity in units", example = "10")
        Integer quantity,

        @Schema(description = "Agreed unit price at time of order creation", example = "18500.00")
        BigDecimal unitPrice,

        @Schema(description = "Calculated line item subtotal (quantity * unitPrice)", example = "185000.00")
        BigDecimal subtotal
) {
}
