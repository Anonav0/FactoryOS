package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Supplier profile and metadata")
public record SupplierResponse(
        @Schema(description = "Unique database supplier ID", example = "1")
        Long id,

        @Schema(description = "Supplier company name", example = "ABC Industrial Supplies")
        String name,

        @Schema(description = "Primary contact person name", example = "Rajesh Kumar")
        String contactPerson,

        @Schema(description = "Corporate contact email", example = "sales@abc-industrial.example")
        String email,

        @Schema(description = "Telephone or mobile contact", example = "+91 98765 43210")
        String phone,

        @Schema(description = "Physical address", example = "12 Industrial Estate, Sector 5, Kolkata, West Bengal")
        String address,

        @Schema(description = "Active status flag (inactive suppliers cannot receive new purchase orders)", example = "true")
        Boolean active,

        @Schema(description = "UTC creation timestamp", example = "2026-09-11T09:30:00Z")
        Instant createdAt,

        @Schema(description = "UTC last update timestamp", example = "2026-09-11T10:00:00Z")
        Instant updatedAt
) {}
