package com.factoryos.service;

import com.factoryos.dto.CreatePurchaseOrderRequest;
import com.factoryos.dto.PurchaseOrderItemRequest;
import com.factoryos.dto.PurchaseOrderResponse;
import com.factoryos.dto.PurchaseOrderSummaryResponse;
import com.factoryos.entity.Product;
import com.factoryos.entity.PurchaseOrder;
import com.factoryos.entity.PurchaseOrderItem;
import com.factoryos.entity.PurchaseOrderStatus;
import com.factoryos.entity.Supplier;
import com.factoryos.exception.BusinessRuleException;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.mapper.PurchaseOrderMapper;
import com.factoryos.repository.ProductRepository;
import com.factoryos.repository.PurchaseOrderRepository;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
    @DisplayName("Should throw BusinessRuleException when items list is empty")
    void createPurchaseOrder_emptyItems() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(activeSupplier));

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                1L,
                LocalDate.now().plusDays(7),
                Collections.emptyList()
        );

        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Purchase order must contain at least one item");

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

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    void createPurchaseOrder_productNotFound() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(activeSupplier));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                1L,
                LocalDate.now().plusDays(7),
                List.of(new PurchaseOrderItemRequest(999L, 10, new BigDecimal("100.00")))
        );

        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when product is inactive")
    void createPurchaseOrder_inactiveProduct() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(activeSupplier));
        when(productRepository.findById(30L)).thenReturn(Optional.of(inactiveProduct));

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                1L,
                LocalDate.now().plusDays(7),
                List.of(new PurchaseOrderItemRequest(30L, 10, new BigDecimal("100.00")))
        );

        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot add inactive product to purchase order: OLD-001");

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when item quantity is zero or negative")
    void createPurchaseOrder_invalidQuantity() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(activeSupplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(activeProduct1));

        CreatePurchaseOrderRequest requestZero = new CreatePurchaseOrderRequest(
                1L,
                LocalDate.now().plusDays(7),
                List.of(new PurchaseOrderItemRequest(10L, 0, new BigDecimal("100.00")))
        );

        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(requestZero))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Quantity must be greater than 0");
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when unit price is negative")
    void createPurchaseOrder_negativeUnitPrice() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(activeSupplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(activeProduct1));

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                1L,
                LocalDate.now().plusDays(7),
                List.of(new PurchaseOrderItemRequest(10L, 10, new BigDecimal("-15.00")))
        );

        assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Unit price cannot be negative");
    }

    @Test
    @DisplayName("Should return all purchase order summaries sorted by creation desc")
    void getAllPurchaseOrders_success() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.CREATED)
                .orderDate(LocalDate.now())
                .totalAmount(new BigDecimal("1000.00"))
                .build();

        when(purchaseOrderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(po));

        List<PurchaseOrderSummaryResponse> list = purchaseOrderService.getAllPurchaseOrders();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).orderNumber()).isEqualTo("PO-000001");
        assertThat(list.get(0).supplierName()).isEqualTo("Apex Tools");
    }

    @Test
    @DisplayName("Should retrieve purchase order by ID")
    void getPurchaseOrderById_success() {
        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L)
                .orderNumber("PO-000001")
                .supplier(activeSupplier)
                .status(PurchaseOrderStatus.CREATED)
                .orderDate(LocalDate.now())
                .totalAmount(new BigDecimal("1000.00"))
                .build();

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));

        PurchaseOrderResponse response = purchaseOrderService.getPurchaseOrderById(1L);

        assertThat(response).isNotNull();
        assertThat(response.orderNumber()).isEqualTo("PO-000001");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when purchase order not found by ID")
    void getPurchaseOrderById_notFound() {
        when(purchaseOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseOrderService.getPurchaseOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Purchase order not found with id: 999");
    }
}

