package com.factoryos.dto;

public record LowStockResponse(
        Long productId,
        String sku,
        String productName,
        Integer quantityAvailable,
        Integer reorderLevel,
        Boolean lowStock
) {}

