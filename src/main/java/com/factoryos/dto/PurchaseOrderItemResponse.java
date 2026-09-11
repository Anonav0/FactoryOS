package com.factoryos.dto;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}

