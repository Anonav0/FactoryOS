package com.factoryos.mapper;

import com.factoryos.dto.CreateProductRequest;
import com.factoryos.dto.ProductResponse;
import com.factoryos.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request) {
        if (request == null) {
            return null;
        }
        return Product.builder()
                .sku(request.sku() != null ? request.sku().trim().toUpperCase() : null)
                .name(request.name() != null ? request.name().trim() : null)
                .description(request.description())
                .category(request.category() != null ? request.category().trim() : null)
                .unitPrice(request.unitPrice())
                .reorderLevel(request.reorderLevel())
                .active(request.active() != null ? request.active() : true)
                .build();
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getUnitPrice(),
                product.getReorderLevel(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}

