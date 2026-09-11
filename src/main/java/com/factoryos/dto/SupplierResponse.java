package com.factoryos.dto;

import java.time.Instant;

public record SupplierResponse(
        Long id,
        String name,
        String contactPerson,
        String email,
        String phone,
        String address,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {}

