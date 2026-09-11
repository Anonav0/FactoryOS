package com.factoryos.dto;

import com.factoryos.entity.PurchaseOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Lightweight Purchase Order summary record for list views")
public record PurchaseOrderSummaryResponse(
        @Schema(description = "Unique purchase order ID", example = "1")
        Long id,

        @Schema(description = "System-generated sequential order tracking number", example = "PO-000001")
        String orderNumber,

        @Schema(description = "Associated supplier ID", example = "1")
        Long supplierId,

        @Schema(description = "Associated supplier legal name", example = "ABC Industrial Supplies")
        String supplierName,

        @Schema(description = "Lifecycle state: CREATED, APPROVED, RECEIVED, or CANCELLED", example = "CREATED")
        PurchaseOrderStatus status,

        @Schema(description = "Date order was created (YYYY-MM-DD)", example = "2026-09-11")
        LocalDate orderDate,

        @Schema(description = "Expected delivery date (YYYY-MM-DD)", example = "2026-09-25")
        LocalDate expectedDeliveryDate,

        @Schema(description = "Total number of distinct product lines in the order", example = "2")
        Integer itemCount,

        @Schema(description = "Calculated total financial value", example = "269000.00")
        BigDecimal totalAmount,

        @Schema(description = "UTC creation timestamp", example = "2026-09-11T09:00:00Z")
        Instant createdAt
) {
}
