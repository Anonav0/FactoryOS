package com.factoryos.dto;

import java.time.Instant;

public record InventoryResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        Integer quantityAvailable,
        Integer reservedQuantity,
        Boolean lowStock,
        Instant lastUpdated
) {}

