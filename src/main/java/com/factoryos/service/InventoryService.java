package com.factoryos.service;

import com.factoryos.dto.InventoryResponse;
import com.factoryos.dto.LowStockResponse;
import com.factoryos.dto.StockAdjustmentRequest;
import com.factoryos.dto.StockInRequest;
import com.factoryos.dto.StockMovementResponse;
import com.factoryos.dto.StockOutRequest;
import com.factoryos.entity.Inventory;
import com.factoryos.entity.Product;

import java.util.List;

public interface InventoryService {

    InventoryResponse stockIn(StockInRequest request);

    InventoryResponse stockOut(StockOutRequest request);

    InventoryResponse adjustStock(StockAdjustmentRequest request);

    List<InventoryResponse> getAllInventory();

    InventoryResponse getInventoryByProductId(Long productId);

    List<StockMovementResponse> getStockMovements(Long productId);

    List<LowStockResponse> getLowStockInventory();

    Inventory initializeInventory(Product product);
}

