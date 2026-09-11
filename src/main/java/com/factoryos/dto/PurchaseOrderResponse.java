package com.factoryos.dto;

import com.factoryos.entity.PurchaseOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Full purchase order representation with associated line items")
public record PurchaseOrderResponse(
        @Schema(description = "Unique database purchase order ID", example = "1")
        Long id,

        @Schema(description = "System-generated sequential order tracking number", example = "PO-000001")
        String orderNumber,

        @Schema(description = "Associated supplier ID", example = "1")
        Long supplierId,

        @Schema(description = "Associated supplier legal name", example = "ABC Industrial Supplies")
        String supplierName,

        @Schema(description = "Lifecycle state: CREATED, APPROVED, RECEIVED, or CANCELLED", example = "CREATED")
        PurchaseOrderStatus status,

        @Schema(description = "Date order was generated (YYYY-MM-DD)", example = "2026-09-11")
        LocalDate orderDate,

        @Schema(description = "Expected delivery date (YYYY-MM-DD)", example = "2026-09-25")
        LocalDate expectedDeliveryDate,

        @Schema(description = "Calculated total financial amount for the order", example = "269000.00")
        BigDecimal totalAmount,

        @Schema(description = "Array of detailed product line items")
        List<PurchaseOrderItemResponse> items,

        @Schema(description = "UTC creation timestamp", example = "2026-09-11T09:00:00Z")
        Instant createdAt,

        @Schema(description = "UTC last update timestamp", example = "2026-09-11T09:00:00Z")
        Instant updatedAt
) {
}
