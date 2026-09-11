package com.factoryos.controller;

import com.factoryos.dto.InventoryResponse;
import com.factoryos.dto.LowStockResponse;
import com.factoryos.dto.StockAdjustmentRequest;
import com.factoryos.dto.StockInRequest;
import com.factoryos.dto.StockMovementResponse;
import com.factoryos.dto.StockOutRequest;
import com.factoryos.exception.ErrorResponse;
import com.factoryos.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Validated
@Tag(name = "Inventory", description = "Real-time stock tracking, atomic stock-in/stock-out operations, adjustments, and low-stock monitoring")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/stock-in")
    @Operation(
            summary = "Add stock to inventory (Stock In)",
            description = "Increments available physical stock for an active product by the specified quantity. Creates an immutable STOCK_IN audit movement record. The operation executes in an atomic transaction."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock successfully received and added to inventory",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g. quantity <= 0 or missing product ID)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product or inventory record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Product is inactive or business rule violated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InventoryResponse> stockIn(@Valid @RequestBody StockInRequest request) {
        return ResponseEntity.ok(inventoryService.stockIn(request));
    }

    @PostMapping("/stock-out")
    @Operation(
            summary = "Deduct stock from inventory (Stock Out)",
            description = "Decrements available stock for production consumption or order dispatch. Available stock cannot become negative; if quantity exceeds quantityAvailable, the transaction fails with 409 Conflict. Creates a STOCK_OUT audit record."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock successfully deducted",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g. quantity <= 0)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product or inventory not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient stock available to complete deduction, or product is inactive",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InventoryResponse> stockOut(@Valid @RequestBody StockOutRequest request) {
        return ResponseEntity.ok(inventoryService.stockOut(request));
    }

    @PostMapping("/adjust")
    @Operation(
            summary = "Reconcile warehouse inventory (Stock Adjustment)",
            description = "Reconciles available stock to a new exact physical count (e.g. after a physical cycle count audit). Calculates the positive or negative delta and logs an ADJUSTMENT movement. Quantity must be >= 0."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock successfully adjusted",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g. newQuantity < 0)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product or inventory record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Product is inactive",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InventoryResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(request));
    }

    @GetMapping
    @Operation(
            summary = "List all inventory records",
            description = "Retrieves current inventory levels, allocated stock, and low-stock flags across all products."
    )
    @ApiResponse(responseCode = "200", description = "List of inventory records retrieved")
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{productId}")
    @Operation(
            summary = "Get inventory by product ID",
            description = "Retrieves real-time inventory level, reserved quantities, and low-stock status for a single product."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory record retrieved",
                    content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid product ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventory record not found for this product",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InventoryResponse> getInventoryByProductId(
            @Parameter(description = "Product ID to look up", example = "1")
            @PathVariable @Positive(message = "Product ID must be positive") Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @GetMapping("/{productId}/movements")
    @Operation(
            summary = "Get stock movement history for product",
            description = "Retrieves the immutable audit trail of all stock transactions (STOCK_IN, STOCK_OUT, ADJUSTMENT) for the specified product, ordered chronologically descending."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock movements retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<StockMovementResponse>> getStockMovements(
            @Parameter(description = "Target product ID", example = "1")
            @PathVariable @Positive(message = "Product ID must be positive") Long productId) {
        return ResponseEntity.ok(inventoryService.getStockMovements(productId));
    }

    @GetMapping("/low-stock")
    @Operation(
            summary = "List low-stock alerts",
            description = "Retrieves all products where current available stock is less than or equal to the product reorder threshold (quantityAvailable <= reorderLevel)."
    )
    @ApiResponse(responseCode = "200", description = "List of low-stock items retrieved successfully")
    public ResponseEntity<List<LowStockResponse>> getLowStockInventory() {
        return ResponseEntity.ok(inventoryService.getLowStockInventory());
    }
}
