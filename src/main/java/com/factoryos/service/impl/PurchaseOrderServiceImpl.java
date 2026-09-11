package com.factoryos.service.impl;

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
import com.factoryos.service.PurchaseOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseOrderServiceImpl.class);

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;

    public PurchaseOrderServiceImpl(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            PurchaseOrderMapper purchaseOrderMapper
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
    }

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) {
        // 1. Validate supplier existence and active state
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.supplierId()));

        if (Boolean.FALSE.equals(supplier.getActive())) {
            throw new BusinessRuleException("Cannot create purchase order for inactive supplier: " + supplier.getName());
        }

        // 2. Validate items collection
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessRuleException("Purchase order must contain at least one item");
        }

        // 3. Prevent duplicate products within the same purchase order
        Set<Long> seenProductIds = new HashSet<>();
        for (PurchaseOrderItemRequest itemReq : request.items()) {
            if (!seenProductIds.add(itemReq.productId())) {
                throw new BusinessRuleException("Duplicate product found in purchase order items: product ID " + itemReq.productId());
            }
        }

        // 4. Generate unique sequential order number
        String orderNumber = generateUniqueOrderNumber();

        // 5. Initialize PurchaseOrder root
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .orderNumber(orderNumber)
                .supplier(supplier)
                .status(PurchaseOrderStatus.CREATED)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(request.expectedDeliveryDate())
                .totalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .build();

        BigDecimal runningTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // 6. Process each line item
        for (PurchaseOrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.productId()));

            if (Boolean.FALSE.equals(product.getActive())) {
                throw new BusinessRuleException("Cannot add inactive product to purchase order: " + product.getSku());
            }

            if (itemReq.quantity() == null || itemReq.quantity() <= 0) {
                throw new BusinessRuleException("Quantity must be greater than 0 for product: " + product.getSku());
            }

            if (itemReq.unitPrice() == null || itemReq.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuleException("Unit price cannot be negative for product: " + product.getSku());
            }

            BigDecimal unitPrice = itemReq.unitPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            purchaseOrder.addItem(item);
            runningTotal = runningTotal.add(subtotal);
        }

        purchaseOrder.setTotalAmount(runningTotal);

        // 7. Persist order and cascade save items
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        log.info("Created purchase order '{}' for supplier '{}' with {} items (Total: {})",
                saved.getOrderNumber(), supplier.getName(), saved.getItems().size(), saved.getTotalAmount());
        return purchaseOrderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderSummaryResponse> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(purchaseOrderMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        return purchaseOrderMapper.toResponse(po);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderByOrderNumber(String orderNumber) {
        PurchaseOrder po = purchaseOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with order number: " + orderNumber));
        return purchaseOrderMapper.toResponse(po);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        // State machine transition validation
        if (po.getStatus() == PurchaseOrderStatus.APPROVED) {
            throw new InvalidPurchaseOrderStateException("Purchase order " + po.getOrderNumber() + " is already approved");
        }
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new InvalidPurchaseOrderStateException("Cannot approve purchase order " + po.getOrderNumber() + " because it is already received");
        }
        if (po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new InvalidPurchaseOrderStateException("Cannot approve purchase order " + po.getOrderNumber() + " because it has been cancelled");
        }
        if (po.getStatus() != PurchaseOrderStatus.CREATED) {
            throw new InvalidPurchaseOrderStateException("Purchase order " + po.getOrderNumber() + " cannot be approved from status: " + po.getStatus());
        }

        // Structural integrity verification
        if (Boolean.FALSE.equals(po.getSupplier().getActive())) {
            throw new BusinessRuleException("Cannot approve purchase order for inactive supplier: " + po.getSupplier().getName());
        }

        if (po.getItems() == null || po.getItems().isEmpty()) {
            throw new BusinessRuleException("Cannot approve purchase order with no line items");
        }

        for (PurchaseOrderItem item : po.getItems()) {
            if (Boolean.FALSE.equals(item.getProduct().getActive())) {
                throw new BusinessRuleException("Cannot approve purchase order containing inactive product: " + item.getProduct().getSku());
            }
        }

        po.setStatus(PurchaseOrderStatus.APPROVED);
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        log.info("Purchase order '{}' approved", po.getOrderNumber());
        return purchaseOrderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse receivePurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        // State machine transition validation
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new InvalidPurchaseOrderStateException("Purchase order " + po.getOrderNumber() + " has already been received");
        }
        if (po.getStatus() == PurchaseOrderStatus.CREATED) {
            throw new InvalidPurchaseOrderStateException("Purchase order " + po.getOrderNumber() + " cannot be received because its current status is CREATED. Only APPROVED purchase orders can be received.");
        }
        if (po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new InvalidPurchaseOrderStateException("Cannot receive purchase order " + po.getOrderNumber() + " because it has been cancelled");
        }
        if (po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new InvalidPurchaseOrderStateException("Purchase order " + po.getOrderNumber() + " cannot be received from status: " + po.getStatus());
        }

        if (po.getItems() == null || po.getItems().isEmpty()) {
            throw new BusinessRuleException("Cannot receive purchase order with no line items");
        }

        // Atomic processing of all line items
        for (PurchaseOrderItem item : po.getItems()) {
            Product product = item.getProduct();
            if (Boolean.FALSE.equals(product.getActive())) {
                throw new BusinessRuleException("Cannot receive purchase order containing inactive product: " + product.getSku());
            }

            // 1. Update Inventory for each item
            Inventory inventory = inventoryRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product: " + product.getSku()));

            inventory.setQuantityAvailable(inventory.getQuantityAvailable() + item.getQuantity());
            inventoryRepository.save(inventory);

            // 2. Create StockMovement audit record
            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .movementType(StockMovementType.STOCK_IN)
                    .quantity(item.getQuantity())
                    .reference(po.getOrderNumber())
                    .reason("Purchase order received")
                    .build();

            stockMovementRepository.save(movement);
        }

        // 3. Mark PO as RECEIVED last
        po.setStatus(PurchaseOrderStatus.RECEIVED);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        log.info("Purchase order '{}' received successfully: updated inventory and stock movements for {} items",
                po.getOrderNumber(), po.getItems().size());

        return purchaseOrderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));

        if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new InvalidPurchaseOrderStateException("Cannot cancel purchase order " + po.getOrderNumber() + " because it has already been received");
        }
        if (po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new InvalidPurchaseOrderStateException("Purchase order " + po.getOrderNumber() + " is already cancelled");
        }
        if (po.getStatus() != PurchaseOrderStatus.CREATED && po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new InvalidPurchaseOrderStateException("Cannot cancel purchase order " + po.getOrderNumber() + " from status: " + po.getStatus());
        }

        // Cancel order — inventory and stock movements remain unchanged
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        log.info("Purchase order '{}' cancelled", po.getOrderNumber());

        return purchaseOrderMapper.toResponse(saved);
    }

    private String generateUniqueOrderNumber() {
        Long nextId = purchaseOrderRepository.getMaxId() + 1;
        String candidate = String.format("PO-%06d", nextId);
        while (purchaseOrderRepository.existsByOrderNumber(candidate)) {
            nextId++;
            candidate = String.format("PO-%06d", nextId);
        }
        return candidate;
    }
}
