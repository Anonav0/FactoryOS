package com.factoryos.service.impl;

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
import com.factoryos.service.PurchaseOrderService;
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

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;

    public PurchaseOrderServiceImpl(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            PurchaseOrderMapper purchaseOrderMapper
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
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

