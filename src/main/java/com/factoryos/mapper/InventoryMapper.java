package com.factoryos.mapper;

import com.factoryos.dto.InventoryResponse;
import com.factoryos.dto.LowStockResponse;
import com.factoryos.entity.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponse toResponse(Inventory inventory) {
        if (inventory == null) {
            return null;
        }

        boolean lowStock = false;
        if (inventory.getProduct() != null && inventory.getProduct().getReorderLevel() != null) {
            lowStock = inventory.getQuantityAvailable() <= inventory.getProduct().getReorderLevel();
        }

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct() != null ? inventory.getProduct().getId() : null,
                inventory.getProduct() != null ? inventory.getProduct().getSku() : null,
                inventory.getProduct() != null ? inventory.getProduct().getName() : null,
                inventory.getQuantityAvailable(),
                inventory.getReservedQuantity(),
                lowStock,
                inventory.getLastUpdated()
        );
    }

    public LowStockResponse toLowStockResponse(Inventory inventory) {
        if (inventory == null) {
            return null;
        }

        return new LowStockResponse(
                inventory.getProduct() != null ? inventory.getProduct().getId() : null,
                inventory.getProduct() != null ? inventory.getProduct().getSku() : null,
                inventory.getProduct() != null ? inventory.getProduct().getName() : null,
                inventory.getQuantityAvailable(),
                inventory.getProduct() != null ? inventory.getProduct().getReorderLevel() : null,
                true
        );
    }
}

