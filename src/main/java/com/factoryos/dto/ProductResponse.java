package com.factoryos.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        String category,
        BigDecimal unitPrice,
        Integer reorderLevel,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {}

