package com.factoryos.service;

import com.factoryos.dto.CreatePurchaseOrderRequest;
import com.factoryos.dto.PurchaseOrderItemRequest;
import com.factoryos.dto.PurchaseOrderResponse;
import com.factoryos.dto.PurchaseOrderSummaryResponse;
import com.factoryos.entity.Inventory;
import com.factoryos.entity.Product;
import com.factoryos.entity.PurchaseOrder;
import com.factoryos.entity.PurchaseOrderItem;
import com.factoryos.entity.PurchaseOrderStatus;
import com.factoryos.entity.StockMovement;
import com.factoryos.entity.StockMovementType;
import com.factoryos.entity.Supplier;
import com.factoryos.exception.BusinessRuleException;
import com.factoryos.exception.InvalidPurchaseOrderStateException;
import com.factoryos.exception.InventoryNotFoundException;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.mapper.PurchaseOrderMapper;
import com.factoryos.repository.InventoryRepository;
import com.factoryos.repository.ProductRepository;
import com.factoryos.repository.PurchaseOrderRepository;
import com.factoryos.repository.StockMovementRepository;
import com.factoryos.repository.SupplierRepository;
import com.factoryos.service.impl.PurchaseOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Spy
    private PurchaseOrderMapper purchaseOrderMapper = new PurchaseOrderMapper();

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    private Supplier activeSupplier;
    private Supplier inactiveSupplier;
    private Product activeProduct1;
    private Product activeProduct2;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        activeSupplier = Supplier.builder()
                .id(1L)
                .name("Apex Tools")
                .active(true)
                .build();

        inactiveSupplier = Supplier.builder()
                .id(2L)
                .name("Defunct Supplies")
                .active(false)
                .build();

        activeProduct1 = Product.builder()
                .id(10L)
                .sku("BRG-6204")
                .name("Steel Bearing 6204")
                .unitPrice(new BigDecimal("450.00"))
                .active(true)
                .build();

        activeProduct2 = Product.builder()
                .id(20L)
                .sku("VLV-1001")
                .name("Ball Valve 1/2 Inch")
                .unitPrice(new BigDecimal("280.50"))
                .active(true)
                .build();

        inactiveProduct = Product.builder()
                .id(30L)
                .sku("OLD-001")
                .name("Deprecated Motor")
                .unitPrice(new BigDecimal("1200.00"))
                .active(false)
                .build();
    }

    @Test
    @DisplayName("Should create purchase order successfully with multiple products and correct subtotal/total calculations")
    void createPurchaseOrder_success_multipleProducts() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(activeSupplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(activeProduct1));
        when(productRepository.findById(20L)).thenReturn(Optional.of(activeProduct2));
        when(purchaseOrderRepository.getMaxId()).thenReturn(0L);
        when(purchaseOrderRepository.existsByOrderNumber("PO-000001")).thenReturn(false);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            po.setId(100L);
            return po;
        });

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                1L,
                LocalDate.now().plusDays(14),
                List.of(
                        new PurchaseOrderItemRequest(10L, 100, new BigDecimal("450.00")),
                        new PurchaseOrderItemRequest(20L, 50, new BigDecimal("280.50"))
                )
        );

        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.orderNumber()).isEqualTo("PO-000001");
        assertThat(response.supplierId()).isEqualTo(1L);
        assertThat(response.supplierName()).isEqualTo("Apex Tools");
        assertThat(response.status()).isEqualTo(PurchaseOrderStatus.CREATED);
        assertThat(response.items()).hasSize(2);

        // Subtotal 1: 100 * 450.00 = 45000.00
        assertThat(response.items().get(0).subtotal()).isEqualByComparingTo("45000.00");
        // Subtotal 2: 50 * 280.50 = 14025.00
        assertThat(response.items().get(1).subtotal()).isEqualByComparingTo("14025.00");
        // Total: 45000.00 + 14025.00 = 59025.00
        assertThat(response.totalAmount()).isEqualByComparingTo("59025.00");

        ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(purchaseOrderRepository).save(captor.capture());
        PurchaseOrder captured = captor.getValue();
        assertThat(captured.getStatus()).isEqualTo(PurchaseOrderStatus.CREATED);
        assertThat(captured.getTotalAmount()).isEqualByComparingTo("59025.00");

        // Verify Inventory Isolation: no inventory updates on PO creation
        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when supplier does not exist")
    void createPurchaseOrder_supplierNotFound() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                99L,
                LocalDate.now().plusDays(7),
                List.of(new PurchaseOrderItemRequest(10L, 10, new BigDecimal("100.00")))
        );

        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with id: 99");

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when supplier is inactive")
    void createPurchaseOrder_inactiveSupplier() {
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(inactiveSupplier));

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                2L,
                LocalDate.now().plusDays(7),
                List.of(new PurchaseOrderItemRequest(10L, 10, new BigDecimal("100.00")))
        );

        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot create purchase order for inactive supplier");

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when duplicate products are in the items list")
    void createPurchaseOrder_duplicateProductsInOrder() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(activeSupplier));

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                1L,
                LocalDate.now().plusDays(7),
                List.of(
                        new PurchaseOrderItemRequest(10L, 10, new BigDecimal("100.00")),
                        new PurchaseOrderItemRequest(10L, 5, new BigDecimal("100.00"))
                )
        );

        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Duplicate product found in purchase order items");

        verify(purchaseOrderRepository, never()).save(any());
    }

    // ==================== State Transition: Approve ====================

    @Test
    @DisplayName("Should successfully approve a CREATED purchase order")
    void approvePurchaseOrder_success() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.CREATED)
                .build();
        po.addItem(PurchaseOrderItem.builder().product(activeProduct1).quantity(10).unitPrice(new BigDecimal("100.00")).subtotal(new BigDecimal("1000.00")).build());

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));

        PurchaseOrderResponse response = purchaseOrderService.approvePurchaseOrder(1L);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PurchaseOrderStatus.APPROVED);
        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.APPROVED);
        verify(purchaseOrderRepository).save(po);
    }

    @Test
    @DisplayName("Should reject approval when purchase order is already APPROVED (409)")
    void approvePurchaseOrder_alreadyApproved_throwsConflict() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.APPROVED)
                .build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> purchaseOrderService.approvePurchaseOrder(1L))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("already approved");

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject approval when purchase order is CANCELLED (409)")
    void approvePurchaseOrder_cancelled_throwsConflict() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.CANCELLED)
                .build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> purchaseOrderService.approvePurchaseOrder(1L))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("because it has been cancelled");
    }

    // ==================== State Transition: Receive ====================

    @Test
    @DisplayName("Should successfully receive an APPROVED PO, update inventory balances, and create STOCK_IN movements")
    void receivePurchaseOrder_success_updatesInventoryAndCreatesMovements() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.APPROVED)
                .build();

        po.addItem(PurchaseOrderItem.builder().product(activeProduct1).quantity(50).unitPrice(new BigDecimal("450.00")).subtotal(new BigDecimal("22500.00")).build());
        po.addItem(PurchaseOrderItem.builder().product(activeProduct2).quantity(20).unitPrice(new BigDecimal("280.50")).subtotal(new BigDecimal("5610.00")).build());

        Inventory inv1 = Inventory.builder().id(101L).product(activeProduct1).quantityAvailable(100).reservedQuantity(0).build();
        Inventory inv2 = Inventory.builder().id(102L).product(activeProduct2).quantityAvailable(40).reservedQuantity(0).build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(inv1));
        when(inventoryRepository.findByProductId(20L)).thenReturn(Optional.of(inv2));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));

        PurchaseOrderResponse response = purchaseOrderService.receivePurchaseOrder(1L);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PurchaseOrderStatus.RECEIVED);
        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);

        // Verify inventory balances increased correctly
        assertThat(inv1.getQuantityAvailable()).isEqualTo(150); // 100 + 50
        assertThat(inv2.getQuantityAvailable()).isEqualTo(60);  // 40 + 20
        verify(inventoryRepository, times(2)).save(any(Inventory.class));

        // Verify StockMovements created
        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository, times(2)).save(movementCaptor.capture());
        List<StockMovement> savedMovements = movementCaptor.getAllValues();

        assertThat(savedMovements).hasSize(2);
        assertThat(savedMovements.get(0).getMovementType()).isEqualTo(StockMovementType.STOCK_IN);
        assertThat(savedMovements.get(0).getQuantity()).isEqualTo(50);
        assertThat(savedMovements.get(0).getReference()).isEqualTo("PO-000001");
        assertThat(savedMovements.get(0).getReason()).isEqualTo("Purchase order received");

        assertThat(savedMovements.get(1).getMovementType()).isEqualTo(StockMovementType.STOCK_IN);
        assertThat(savedMovements.get(1).getQuantity()).isEqualTo(20);
        assertThat(savedMovements.get(1).getReference()).isEqualTo("PO-000001");
    }

    @Test
    @DisplayName("Should reject receiving when PO is in CREATED status (cannot bypass approval)")
    void receivePurchaseOrder_unapproved_throwsConflict() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.CREATED)
                .build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(1L))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("Only APPROVED purchase orders can be received");

        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject receiving when PO is already RECEIVED (duplicate receive prevention)")
    void receivePurchaseOrder_alreadyReceived_throwsConflict() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.RECEIVED)
                .build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(1L))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("has already been received");

        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InventoryNotFoundException and not mark received if inventory record is missing")
    void receivePurchaseOrder_missingInventory_throwsException() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.APPROVED)
                .build();
        po.addItem(PurchaseOrderItem.builder().product(activeProduct1).quantity(10).unitPrice(new BigDecimal("100.00")).subtotal(new BigDecimal("1000.00")).build());

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(1L))
                .isInstanceOf(InventoryNotFoundException.class)
                .hasMessageContaining("Inventory not found for product: BRG-6204");

        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.APPROVED);
        verify(purchaseOrderRepository, never()).save(po);
    }

    // ==================== State Transition: Cancel ====================

    @Test
    @DisplayName("Should successfully cancel a CREATED purchase order without touching inventory")
    void cancelPurchaseOrder_fromCreated_success() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.CREATED)
                .build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));

        PurchaseOrderResponse response = purchaseOrderService.cancelPurchaseOrder(1L);

        assertThat(response.status()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);

        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully cancel an APPROVED purchase order without touching inventory")
    void cancelPurchaseOrder_fromApproved_success() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.APPROVED)
                .build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));

        PurchaseOrderResponse response = purchaseOrderService.cancelPurchaseOrder(1L);

        assertThat(response.status()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);

        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject cancellation when PO is already RECEIVED (409)")
    void cancelPurchaseOrder_received_throwsConflict() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.RECEIVED)
                .build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> purchaseOrderService.cancelPurchaseOrder(1L))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("because it has already been received");

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject cancellation when PO is already CANCELLED (409)")
    void cancelPurchaseOrder_alreadyCancelled_throwsConflict() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.CANCELLED)
                .build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> purchaseOrderService.cancelPurchaseOrder(1L))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("is already cancelled");
    }
}
