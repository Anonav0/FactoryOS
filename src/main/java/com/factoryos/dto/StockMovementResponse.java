package com.factoryos.dto;

import com.factoryos.entity.StockMovementType;

import java.time.Instant;

public record StockMovementResponse(
        Long id,
        Long productId,
        StockMovementType movementType,
        Integer quantity,
        String reference,
        String reason,
        Instant createdAt
) {}

