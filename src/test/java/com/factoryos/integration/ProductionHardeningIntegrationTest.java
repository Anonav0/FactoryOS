package com.factoryos.integration;

import com.factoryos.entity.Inventory;
import com.factoryos.entity.Product;
import com.factoryos.repository.InventoryRepository;
import com.factoryos.repository.ProductRepository;
import com.factoryos.repository.PurchaseOrderItemRepository;
import com.factoryos.repository.PurchaseOrderRepository;
import com.factoryos.repository.StockMovementRepository;
import com.factoryos.repository.SupplierRepository;
import com.factoryos.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProductionHardeningIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

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
    @DisplayName("Optimistic Locking: Stale concurrent inventory modification throws ObjectOptimisticLockingFailureException")
    void optimisticLocking_concurrentUpdate_throwsConflict() {
        Product product = productRepository.save(
                TestDataFactory.createProduct("OPT-LOCK-001", "Optimistic Lock Test Product", new BigDecimal("100.00"), 5, true)
        );

        Inventory inventory = inventoryRepository.save(
                Inventory.builder()
                        .product(product)
                        .quantityAvailable(50)
                        .reservedQuantity(0)
                        .build()
        );

        // Fetch two distinct entity representations representing concurrent transactions
        Inventory tx1Instance = inventoryRepository.findById(inventory.getId()).orElseThrow();
        Inventory tx2Instance = inventoryRepository.findById(inventory.getId()).orElseThrow();

        // Transaction 1 modifies and commits first
        tx1Instance.setQuantityAvailable(60);
        inventoryRepository.saveAndFlush(tx1Instance);

        // Transaction 2 tries to commit based on stale version
        tx2Instance.setQuantityAvailable(70);
        assertThatThrownBy(() -> inventoryRepository.saveAndFlush(tx2Instance))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("Database Constraints: Duplicate product SKU throws DataIntegrityViolationException")
    void duplicateSku_violatesUniqueConstraint() {
        Product p1 = TestDataFactory.createProduct("HARDEN-001", "Original Product", new BigDecimal("50.00"), 5, true);
        productRepository.saveAndFlush(p1);

        Product p2 = TestDataFactory.createProduct("HARDEN-001", "Duplicate SKU Product", new BigDecimal("75.00"), 10, true);

        assertThatThrownBy(() -> productRepository.saveAndFlush(p2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

