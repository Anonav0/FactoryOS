package com.factoryos.dto;

import com.factoryos.entity.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PurchaseOrderSummaryResponse(
        Long id,
        String orderNumber,
        Long supplierId,
        String supplierName,
        PurchaseOrderStatus status,
        LocalDate orderDate,
        LocalDate expectedDeliveryDate,
        Integer itemCount,
        BigDecimal totalAmount,
        Instant createdAt
) {
}

