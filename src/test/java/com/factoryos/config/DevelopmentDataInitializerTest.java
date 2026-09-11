package com.factoryos.config;

import com.factoryos.dto.LowStockResponse;
import com.factoryos.entity.Inventory;
import com.factoryos.entity.Product;
import com.factoryos.entity.PurchaseOrder;
import com.factoryos.entity.PurchaseOrderStatus;
import com.factoryos.repository.InventoryRepository;
import com.factoryos.repository.ProductRepository;
import com.factoryos.repository.PurchaseOrderItemRepository;
import com.factoryos.repository.PurchaseOrderRepository;
import com.factoryos.repository.StockMovementRepository;
import com.factoryos.repository.SupplierRepository;
import com.factoryos.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("dev")
class DevelopmentDataInitializerTest {

    @Autowired
    private DevelopmentDataInitializer initializer;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private InventoryService inventoryService;

    @BeforeEach
    void setupDatabase() {
        stockMovementRepository.deleteAllInBatch();
        purchaseOrderItemRepository.deleteAllInBatch();
        purchaseOrderRepository.deleteAllInBatch();
        inventoryRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        supplierRepository.deleteAllInBatch();

        // Run the initializer to seed demo data
        initializer.run(null);
    }

    @Test
    @DisplayName("Demo Seeding: Initializes 8 realistic industrial products and 5 suppliers")
    void seedsProductsAndSuppliers() {
        assertThat(productRepository.count()).isEqualTo(8);
        assertThat(supplierRepository.count()).isEqualTo(5);

        Product motor = productRepository.findBySku("MOTOR-001").orElseThrow();
        assertThat(motor.getName()).isEqualTo("Industrial Electric Motor");
        assertThat(motor.getActive()).isTrue();
        assertThat(motor.getReorderLevel()).isEqualTo(10);

        Product gear = productRepository.findBySku("GEAR-001").orElseThrow();
        assertThat(gear.getActive()).isTrue();
        assertThat(gear.getReorderLevel()).isEqualTo(8);

        assertThat(supplierRepository.findByNameContainingIgnoreCase("ABC Industrial Supplies")).isNotEmpty();
        assertThat(supplierRepository.findByNameContainingIgnoreCase("Eastern Engineering Components")).isNotEmpty();
    }

    @Test
    @DisplayName("Demo Seeding: Creates inventory with healthy, low-stock, and zero-stock items")
    void seedsInventoryWithLowAndZeroStock() {
        List<LowStockResponse> lowStockItems = inventoryService.getLowStockInventory();
        assertThat(lowStockItems).hasSize(3);

        List<String> lowStockSkus = lowStockItems.stream().map(LowStockResponse::sku).toList();
        assertThat(lowStockSkus).containsExactlyInAnyOrder("PUMP-001", "VALVE-001", "GEAR-001");

        // Verify zero stock item
        Product gear = productRepository.findBySku("GEAR-001").orElseThrow();
        Inventory gearInventory = inventoryRepository.findByProductId(gear.getId()).orElseThrow();
        assertThat(gearInventory.getQuantityAvailable()).isEqualTo(0);

        // Verify healthy stock item
        Product motor = productRepository.findBySku("MOTOR-001").orElseThrow();
        Inventory motorInventory = inventoryRepository.findByProductId(motor.getId()).orElseThrow();
        assertThat(motorInventory.getQuantityAvailable()).isEqualTo(45);
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    @DisplayName("Demo Seeding: Seeds 4 Purchase Orders representing CREATED, APPROVED, RECEIVED, and CANCELLED states")
    void seedsMultiStatePurchaseOrders() {
        assertThat(purchaseOrderRepository.count()).isEqualTo(4);

        PurchaseOrder po1 = purchaseOrderRepository.findByOrderNumber("PO-000001").orElseThrow();
        assertThat(po1.getStatus()).isEqualTo(PurchaseOrderStatus.CREATED);
        assertThat(po1.getItems()).hasSize(2);

        PurchaseOrder po2 = purchaseOrderRepository.findByOrderNumber("PO-000002").orElseThrow();
        assertThat(po2.getStatus()).isEqualTo(PurchaseOrderStatus.APPROVED);

        PurchaseOrder po3 = purchaseOrderRepository.findByOrderNumber("PO-000003").orElseThrow();
        assertThat(po3.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);

        PurchaseOrder po4 = purchaseOrderRepository.findByOrderNumber("PO-000004").orElseThrow();
        assertThat(po4.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Idempotency: Re-running initializer does not duplicate products, suppliers, or orders")
    void seedingIsIdempotent() {
        long initialProductCount = productRepository.count();
        long initialSupplierCount = supplierRepository.count();
        long initialOrderCount = purchaseOrderRepository.count();

        // Run second time
        assertThatCode(() -> initializer.run(null)).doesNotThrowAnyException();

        assertThat(productRepository.count()).isEqualTo(initialProductCount);
        assertThat(supplierRepository.count()).isEqualTo(initialSupplierCount);
        assertThat(purchaseOrderRepository.count()).isEqualTo(initialOrderCount);
    }
}
