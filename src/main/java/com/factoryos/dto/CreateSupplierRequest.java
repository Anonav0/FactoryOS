package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for onboarding a new industrial supplier")
public record CreateSupplierRequest(
        @Schema(description = "Legal company name of the supplier", example = "ABC Industrial Supplies")
        @NotBlank(message = "Supplier name cannot be blank")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        String name,

        @Schema(description = "Primary contact person name", example = "Rajesh Kumar")
        @Size(max = 100, message = "Contact person must not exceed 100 characters")
        String contactPerson,

        @Schema(description = "Supplier corporate email address", example = "sales@abc-industrial.example")
        @Email(message = "Email format is invalid")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Schema(description = "Contact telephone or mobile number", example = "+91 98765 43210")
        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Schema(description = "Physical address or warehouse location", example = "12 Industrial Estate, Sector 5, Kolkata, West Bengal")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @Schema(description = "Active status flag (defaults to true if null)", example = "true")
        Boolean active
) {}
