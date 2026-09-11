package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Request payload for creating a new multi-item Purchase Order")
public record CreatePurchaseOrderRequest(
        @Schema(description = "ID of the active supplier fulfilling the order", example = "1")
        @NotNull(message = "Supplier ID cannot be null")
        Long supplierId,

        @Schema(description = "Estimated arrival or delivery date (YYYY-MM-DD)", example = "2026-09-25")
        LocalDate expectedDeliveryDate,

        @Schema(description = "List of distinct product line items (must contain at least one item without duplicate products)")
        @NotEmpty(message = "Purchase order must contain at least one item")
        @Size(min = 1, max = 100, message = "Purchase order must contain between 1 and 100 items")
        @Valid
        List<PurchaseOrderItemRequest> items
) {
}
