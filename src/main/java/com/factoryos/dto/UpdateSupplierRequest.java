package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for updating supplier profile information")
public record UpdateSupplierRequest(
        @Schema(description = "Updated company name", example = "ABC Industrial Supplies Pvt Ltd")
        @NotBlank(message = "Supplier name cannot be blank")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        String name,

        @Schema(description = "Updated contact person", example = "Rajesh Kumar")
        @Size(max = 100, message = "Contact person must not exceed 100 characters")
        String contactPerson,

        @Schema(description = "Updated email address", example = "rajesh@abc-industrial.example")
        @Email(message = "Email format is invalid")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Schema(description = "Updated phone number", example = "+91 98765 43211")
        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Schema(description = "Updated business address", example = "12 Industrial Estate, Sector 5, Kolkata, West Bengal")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @Schema(description = "Active status flag", example = "true")
        Boolean active
) {}
