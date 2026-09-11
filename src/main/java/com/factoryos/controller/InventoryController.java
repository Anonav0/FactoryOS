package com.factoryos.controller;

import com.factoryos.dto.InventoryResponse;
import com.factoryos.dto.LowStockResponse;
import com.factoryos.dto.StockAdjustmentRequest;
import com.factoryos.dto.StockInRequest;
import com.factoryos.dto.StockMovementResponse;
import com.factoryos.dto.StockOutRequest;
import com.factoryos.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/stock-in")
    public ResponseEntity<InventoryResponse> stockIn(@Valid @RequestBody StockInRequest request) {
        return ResponseEntity.ok(inventoryService.stockIn(request));
    }

    @PostMapping("/stock-out")
    public ResponseEntity<InventoryResponse> stockOut(@Valid @RequestBody StockOutRequest request) {
        return ResponseEntity.ok(inventoryService.stockOut(request));
    }

    @PostMapping("/adjust")
    public ResponseEntity<InventoryResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(request));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @GetMapping("/{productId}/movements")
    public ResponseEntity<List<StockMovementResponse>> getStockMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getStockMovements(productId));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockResponse>> getLowStockInventory() {
        return ResponseEntity.ok(inventoryService.getLowStockInventory());
    }
}

