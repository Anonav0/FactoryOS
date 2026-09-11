package com.factoryos.mapper;

import com.factoryos.dto.CreateSupplierRequest;
import com.factoryos.dto.SupplierResponse;
import com.factoryos.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public Supplier toEntity(CreateSupplierRequest request) {
        if (request == null) {
            return null;
        }
        return Supplier.builder()
                .name(request.name() != null ? request.name().trim() : null)
                .contactPerson(request.contactPerson() != null ? request.contactPerson().trim() : null)
                .email(request.email() != null ? request.email().trim().toLowerCase() : null)
                .phone(request.phone() != null ? request.phone().trim() : null)
                .address(request.address() != null ? request.address().trim() : null)
                .active(request.active() != null ? request.active() : true)
                .build();
    }

    public SupplierResponse toResponse(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}

