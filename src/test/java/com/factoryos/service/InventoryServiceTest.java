package com.factoryos.service;

import com.factoryos.dto.InventoryResponse;
import com.factoryos.dto.LowStockResponse;
import com.factoryos.dto.StockAdjustmentRequest;
import com.factoryos.dto.StockInRequest;
import com.factoryos.dto.StockMovementResponse;
import com.factoryos.dto.StockOutRequest;
import com.factoryos.entity.Inventory;
import com.factoryos.entity.Product;
import com.factoryos.entity.StockMovement;
import com.factoryos.entity.StockMovementType;
import com.factoryos.exception.BusinessRuleException;
import com.factoryos.exception.InsufficientStockException;
import com.factoryos.exception.InventoryNotFoundException;
import com.factoryos.exception.InvalidStockAdjustmentException;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.mapper.InventoryMapper;
import com.factoryos.mapper.StockMovementMapper;
import com.factoryos.repository.InventoryRepository;
import com.factoryos.repository.ProductRepository;
import com.factoryos.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Spy
    private InventoryMapper inventoryMapper = new InventoryMapper();

    @Spy
    private StockMovementMapper stockMovementMapper = new StockMovementMapper();

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product testProduct;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .sku("BRG-6204")
                .name("Steel Bearing 6204")
                .description("Industrial deep-groove bearing")
                .category("Bearings")
                .unitPrice(new BigDecimal("450.00"))
                .reorderLevel(20)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testInventory = Inventory.builder()
                .id(1L)
                .product(testProduct)
                .quantityAvailable(100)
                .reservedQuantity(0)
                .version(0L)
                .lastUpdated(Instant.now())
                .build();
    }

    @Test
    void stockIn_success_increasesQuantityAndRecordsMovement() {
        StockInRequest request = new StockInRequest(1L, 25, "GRN-1001", "Received from supplier");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.stockIn(request);

        assertThat(response.quantityAvailable()).isEqualTo(125);
        assertThat(testInventory.getQuantityAvailable()).isEqualTo(125);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        StockMovement savedMovement = movementCaptor.getValue();
        assertThat(savedMovement.getMovementType()).isEqualTo(StockMovementType.STOCK_IN);
        assertThat(savedMovement.getQuantity()).isEqualTo(25);
        assertThat(savedMovement.getReference()).isEqualTo("GRN-1001");
    }

    @Test
    void stockOut_success_decreasesQuantityAndRecordsMovement() {
        StockOutRequest request = new StockOutRequest(1L, 25, "REQ-2001", "Production usage");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.stockOut(request);

        assertThat(response.quantityAvailable()).isEqualTo(75);
        assertThat(testInventory.getQuantityAvailable()).isEqualTo(75);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        StockMovement savedMovement = movementCaptor.getValue();
        assertThat(savedMovement.getMovementType()).isEqualTo(StockMovementType.STOCK_OUT);
        assertThat(savedMovement.getQuantity()).isEqualTo(25);
    }

    @Test
    void stockOut_insufficientStock_throwsInsufficientStockExceptionAndNoMovementSaved() {
        testInventory.setQuantityAvailable(10);
        StockOutRequest request = new StockOutRequest(1L, 15, "REQ-2002", "Production usage");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));

        assertThatThrownBy(() -> inventoryService.stockOut(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Cannot remove 15 units of BRG-6204. Available stock: 10");

        assertThat(testInventory.getQuantityAvailable()).isEqualTo(10); // Unchanged
        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void stockOut_inactiveProduct_throwsBusinessRuleException() {
        testProduct.setActive(false);
        StockOutRequest request = new StockOutRequest(1L, 5, "REQ-2003", "Usage");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> inventoryService.stockOut(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactive product");

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void adjustStock_success_updatesQuantityAndRecordsMovementWithDelta() {
        StockAdjustmentRequest request = new StockAdjustmentRequest(1L, 95, "AUDIT-01", "Physical count");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.adjustStock(request);

        assertThat(response.quantityAvailable()).isEqualTo(95);
        assertThat(testInventory.getQuantityAvailable()).isEqualTo(95);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        StockMovement savedMovement = movementCaptor.getValue();
        assertThat(savedMovement.getMovementType()).isEqualTo(StockMovementType.ADJUSTMENT);
        assertThat(savedMovement.getQuantity()).isEqualTo(-5); // 95 - 100 = -5
    }

    @Test
    void adjustStock_negativeQuantity_throwsInvalidStockAdjustmentException() {
        StockAdjustmentRequest request = new StockAdjustmentRequest(1L, -5, "AUDIT-02", "Invalid count");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));

        assertThatThrownBy(() -> inventoryService.adjustStock(request))
                .isInstanceOf(InvalidStockAdjustmentException.class)
                .hasMessageContaining("negative");

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void adjustStock_positiveDelta_recordsPositiveMovementQuantity() {
        StockAdjustmentRequest request = new StockAdjustmentRequest(1L, 120, "AUDIT-03", "Found extra units");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.adjustStock(request);

        assertThat(response.quantityAvailable()).isEqualTo(120);
        assertThat(testInventory.getQuantityAvailable()).isEqualTo(120);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        StockMovement savedMovement = movementCaptor.getValue();
        assertThat(savedMovement.getMovementType()).isEqualTo(StockMovementType.ADJUSTMENT);
        assertThat(savedMovement.getQuantity()).isEqualTo(20); // 120 - 100 = 20
    }

    @Test
    void adjustStock_zeroDelta_recordsZeroMovementQuantity() {
        StockAdjustmentRequest request = new StockAdjustmentRequest(1L, 100, "AUDIT-04", "Verified exact count");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponse response = inventoryService.adjustStock(request);

        assertThat(response.quantityAvailable()).isEqualTo(100);
        assertThat(testInventory.getQuantityAvailable()).isEqualTo(100);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        StockMovement savedMovement = movementCaptor.getValue();
        assertThat(savedMovement.getMovementType()).isEqualTo(StockMovementType.ADJUSTMENT);
        assertThat(savedMovement.getQuantity()).isEqualTo(0); // 100 - 100 = 0
    }

    @Test
    void stockIn_inactiveProduct_throwsBusinessRuleException() {
        testProduct.setActive(false);
        StockInRequest request = new StockInRequest(1L, 50, "GRN-01", "Delivery");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> inventoryService.stockIn(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactive");

        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void adjustStock_inactiveProduct_throwsBusinessRuleException() {
        testProduct.setActive(false);
        StockAdjustmentRequest request = new StockAdjustmentRequest(1L, 50, "AUDIT-05", "Recount");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> inventoryService.adjustStock(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactive");

        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void lowStock_detectionLogic_boundaryConditionEqualReorderLevel() {
        testInventory.setQuantityAvailable(20);
        testProduct.setReorderLevel(20);
        when(inventoryRepository.findLowStockInventory()).thenReturn(List.of(testInventory));

        List<LowStockResponse> lowStockList = inventoryService.getLowStockInventory();

        assertThat(lowStockList).hasSize(1);
        assertThat(lowStockList.get(0).lowStock()).isTrue();
    }

    @Test
    void lowStock_detectionLogic_boundaryConditionBelowReorderLevel() {
        testInventory.setQuantityAvailable(19);
        testProduct.setReorderLevel(20);
        when(inventoryRepository.findLowStockInventory()).thenReturn(List.of(testInventory));

        List<LowStockResponse> lowStockList = inventoryService.getLowStockInventory();

        assertThat(lowStockList).hasSize(1);
        assertThat(lowStockList.get(0).lowStock()).isTrue();
    }

    @Test
    void lowStock_detectionLogic_boundaryConditionAboveReorderLevel() {
        when(inventoryRepository.findLowStockInventory()).thenReturn(List.of());

        List<LowStockResponse> lowStockList = inventoryService.getLowStockInventory();

        assertThat(lowStockList).isEmpty();
    }

    @Test
    void getInventoryByProductId_productNotFound_throwsResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryByProductId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getInventoryByProductId_inventoryNotFound_throwsInventoryNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryByProductId(1L))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void initializeInventory_createsInitialRecord() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory created = inventoryService.initializeInventory(testProduct);

        assertThat(created).isNotNull();
        assertThat(created.getQuantityAvailable()).isEqualTo(0);
        assertThat(created.getReservedQuantity()).isEqualTo(0);
        verify(inventoryRepository).save(any(Inventory.class));
    }
}

