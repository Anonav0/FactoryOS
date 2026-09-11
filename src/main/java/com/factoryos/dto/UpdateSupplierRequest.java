package com.factoryos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSupplierRequest(
        @NotBlank(message = "Supplier name cannot be blank")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        String name,

        @Size(max = 100, message = "Contact person must not exceed 100 characters")
        String contactPerson,

        @Email(message = "Email format is invalid")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        Boolean active
) {}

