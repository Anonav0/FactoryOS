package com.factoryos.dto;

import com.factoryos.entity.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        String orderNumber,
        Long supplierId,
        String supplierName,
        PurchaseOrderStatus status,
        LocalDate orderDate,
        LocalDate expectedDeliveryDate,
        BigDecimal totalAmount,
        List<PurchaseOrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}

