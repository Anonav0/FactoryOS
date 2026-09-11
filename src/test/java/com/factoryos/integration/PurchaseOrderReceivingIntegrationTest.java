package com.factoryos.integration;

import com.factoryos.entity.*;
import com.factoryos.exception.BusinessRuleException;
import com.factoryos.repository.*;
import com.factoryos.service.PurchaseOrderService;
import com.factoryos.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PurchaseOrderReceivingIntegrationTest {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

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
    @DisplayName("End-to-End: Receiving approved PO atomically updates inventory, records movements, and sets status RECEIVED")
    void receivePurchaseOrder_successful_updatesInventoryAndMovementsAndStatus() {
        Supplier supplier = supplierRepository.save(
                TestDataFactory.createSupplier("Apex Industrial Tools", "contact@apex.example", true)
        );

        Product prod1 = productRepository.save(
                TestDataFactory.createProduct("BRG-IT-01", "Bearing IT 01", new BigDecimal("100.00"), 10, true)
        );
        Product prod2 = productRepository.save(
                TestDataFactory.createProduct("VALVE-IT-02", "Valve IT 02", new BigDecimal("250.00"), 10, true)
        );

        inventoryRepository.save(TestDataFactory.createInventory(prod1, 50, 0));
        inventoryRepository.save(TestDataFactory.createInventory(prod2, 30, 0));

        PurchaseOrder po = TestDataFactory.createPurchaseOrder(supplier, "PO-INT-001", PurchaseOrderStatus.APPROVED);
        TestDataFactory.createPurchaseOrderItem(po, prod1, 25, new BigDecimal("100.00"));
        TestDataFactory.createPurchaseOrderItem(po, prod2, 15, new BigDecimal("250.00"));
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);

        // Execute goods receipt
        purchaseOrderService.receivePurchaseOrder(savedPo.getId());

        // Verify PO terminal status
        PurchaseOrder receivedPo = purchaseOrderRepository.findById(savedPo.getId()).orElseThrow();
        assertThat(receivedPo.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);

        // Verify Inventory increments
        Inventory inv1 = inventoryRepository.findByProductId(prod1.getId()).orElseThrow();
        assertThat(inv1.getQuantityAvailable()).isEqualTo(75); // 50 + 25

        Inventory inv2 = inventoryRepository.findByProductId(prod2.getId()).orElseThrow();
        assertThat(inv2.getQuantityAvailable()).isEqualTo(45); // 30 + 15

        // Verify Stock Movements
        List<StockMovement> movements = stockMovementRepository.findAll();
        assertThat(movements).hasSize(2);
        assertThat(movements).allMatch(m -> m.getMovementType() == StockMovementType.STOCK_IN);
        assertThat(movements).allMatch(m -> "PO-INT-001".equals(m.getReference()));
        assertThat(movements).allMatch(m -> "Purchase order received".equals(m.getReason()));
    }

    @Test
    @DisplayName("Transaction Rollback: Failure on an item rolls back all inventory changes, movements, and PO status")
    void receivePurchaseOrder_whenItemFails_rollsBackEntireTransactionAtomically() {
        Supplier supplier = supplierRepository.save(
                TestDataFactory.createSupplier("Reliable Hardware", "reliable@example.com", true)
        );

        Product prod1 = productRepository.save(
                TestDataFactory.createProduct("PUMP-RB-01", "Pump Rollback 1", new BigDecimal("500.00"), 10, true)
        );
        Product prod2 = productRepository.save(
                TestDataFactory.createProduct("HOSE-RB-02", "Hose Rollback 2", new BigDecimal("150.00"), 10, true)
        );

        inventoryRepository.save(TestDataFactory.createInventory(prod1, 100, 0));
        inventoryRepository.save(TestDataFactory.createInventory(prod2, 200, 0));

        PurchaseOrder po = TestDataFactory.createPurchaseOrder(supplier, "PO-ROLLBACK-002", PurchaseOrderStatus.APPROVED);
        TestDataFactory.createPurchaseOrderItem(po, prod1, 50, new BigDecimal("500.00"));
        TestDataFactory.createPurchaseOrderItem(po, prod2, 40, new BigDecimal("150.00"));
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);

        // Introduce failure condition: Deactivate prod2 in database before receiving
        prod2.setActive(false);
        productRepository.save(prod2);

        // Attempt receiving, expecting failure
        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(savedPo.getId()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactive product");

        // VERIFY COMPLETE TRANSACTION ROLLBACK:
        // 1. PO status must remain APPROVED
        PurchaseOrder poAfterFailure = purchaseOrderRepository.findById(savedPo.getId()).orElseThrow();
        assertThat(poAfterFailure.getStatus()).isEqualTo(PurchaseOrderStatus.APPROVED);

        // 2. Product 1 inventory MUST NOT have been incremented (remains 100, NOT 150)
        Inventory inv1After = inventoryRepository.findByProductId(prod1.getId()).orElseThrow();
        assertThat(inv1After.getQuantityAvailable()).isEqualTo(100);

        // 3. Product 2 inventory remains 200
        Inventory inv2After = inventoryRepository.findByProductId(prod2.getId()).orElseThrow();
        assertThat(inv2After.getQuantityAvailable()).isEqualTo(200);

        // 4. Zero stock movements must be persisted
        List<StockMovement> movementsAfter = stockMovementRepository.findAll();
        assertThat(movementsAfter).isEmpty();
    }
}
