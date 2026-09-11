package com.factoryos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseOrderRequest(
        @NotNull(message = "Supplier ID cannot be null")
        Long supplierId,

        LocalDate expectedDeliveryDate,

        @NotEmpty(message = "Purchase order must contain at least one item")
        @Valid
        List<PurchaseOrderItemRequest> items
) {
}

