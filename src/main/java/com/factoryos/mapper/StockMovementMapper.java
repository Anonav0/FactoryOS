package com.factoryos.mapper;

import com.factoryos.dto.StockMovementResponse;
import com.factoryos.entity.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementResponse toResponse(StockMovement stockMovement) {
        if (stockMovement == null) {
            return null;
        }

        return new StockMovementResponse(
                stockMovement.getId(),
                stockMovement.getProduct() != null ? stockMovement.getProduct().getId() : null,
                stockMovement.getMovementType(),
                stockMovement.getQuantity(),
                stockMovement.getReference(),
                stockMovement.getReason(),
                stockMovement.getCreatedAt()
        );
    }
}

