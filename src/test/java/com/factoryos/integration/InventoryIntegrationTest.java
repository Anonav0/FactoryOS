package com.factoryos.integration;

import com.factoryos.dto.StockInRequest;
import com.factoryos.dto.StockOutRequest;
import com.factoryos.entity.Inventory;
import com.factoryos.entity.Product;
import com.factoryos.entity.StockMovement;
import com.factoryos.entity.StockMovementType;
import com.factoryos.exception.InsufficientStockException;
import com.factoryos.repository.InventoryRepository;
import com.factoryos.repository.ProductRepository;
import com.factoryos.repository.PurchaseOrderItemRepository;
import com.factoryos.repository.PurchaseOrderRepository;
import com.factoryos.repository.StockMovementRepository;
import com.factoryos.repository.SupplierRepository;
import com.factoryos.service.InventoryService;
import com.factoryos.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class InventoryIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @BeforeEach
    void cleanDatabase() {
        stockMovementRepository.deleteAllInBatch();
        purchaseOrderItemRepository.deleteAllInBatch();
        purchaseOrderRepository.deleteAllInBatch();
        inventoryRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        supplierRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Database Unique Constraint: Attempting to insert duplicate product SKU throws DataIntegrityViolationException")
    void duplicateSku_violatesDatabaseUniqueConstraint() {
        Product prod1 = TestDataFactory.createProduct("SKU-UNIQUE-01", "Product 1", new BigDecimal("100.00"), 10, true);
        productRepository.saveAndFlush(prod1);

        Product prod2 = TestDataFactory.createProduct("SKU-UNIQUE-01", "Product 2 Duplicate", new BigDecimal("200.00"), 10, true);

        assertThatThrownBy(() -> productRepository.saveAndFlush(prod2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Full Inventory Lifecycle: Stock in, stock out, and negative stock rejection against PostgreSQL")
    void inventoryLifecycle_stockInStockOutAndNegativeStockRejection() {
        Product product = productRepository.save(
                TestDataFactory.createProduct("SKU-LIFECYCLE", "Lifecycle Product", new BigDecimal("50.00"), 20, true)
        );
        inventoryRepository.save(TestDataFactory.createInventory(product, 0, 0));

        // 1. Stock In 100 units
        inventoryService.stockIn(new StockInRequest(product.getId(), 100, "GRN-LC-1", "Initial batch"));
        Inventory invAfterIn = inventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(invAfterIn.getQuantityAvailable()).isEqualTo(100);

        // 2. Stock Out 70 units
        inventoryService.stockOut(new StockOutRequest(product.getId(), 70, "REQ-LC-1", "Assembly usage"));
        Inventory invAfterOut = inventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(invAfterOut.getQuantityAvailable()).isEqualTo(30);

        // 3. Stock Out 50 units (exceeds available 30) -> MUST fail
        assertThatThrownBy(() -> inventoryService.stockOut(new StockOutRequest(product.getId(), 50, "REQ-LC-2", "Excessive")))
                .isInstanceOf(InsufficientStockException.class);

        // Verify stock was not changed
        Inventory invAfterFailed = inventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertThat(invAfterFailed.getQuantityAvailable()).isEqualTo(30);

        // Verify movement audit log contains exactly 2 records (STOCK_IN and STOCK_OUT)
        List<StockMovement> movements = stockMovementRepository.findByProductIdOrderByCreatedAtDesc(product.getId());
        assertThat(movements).hasSize(2);
        assertThat(movements.get(0).getMovementType()).isEqualTo(StockMovementType.STOCK_OUT);
        assertThat(movements.get(1).getMovementType()).isEqualTo(StockMovementType.STOCK_IN);
    }
}
