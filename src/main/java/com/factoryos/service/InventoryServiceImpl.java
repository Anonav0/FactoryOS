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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;
    private final StockMovementMapper stockMovementMapper;

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            ProductRepository productRepository,
            InventoryMapper inventoryMapper,
            StockMovementMapper stockMovementMapper
    ) {
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.inventoryMapper = inventoryMapper;
        this.stockMovementMapper = stockMovementMapper;
    }

    @Override
    @Transactional
    public InventoryResponse stockIn(StockInRequest request) {
        Product product = findActiveProductOrThrow(request.productId());
        Inventory inventory = findInventoryOrThrow(product.getId());

        inventory.setQuantityAvailable(inventory.getQuantityAvailable() + request.quantity());
        Inventory updatedInventory = inventoryRepository.save(inventory);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .movementType(StockMovementType.STOCK_IN)
                .quantity(request.quantity())
                .reference(request.reference())
                .reason(request.reason())
                .build();
        stockMovementRepository.save(movement);

        log.info("Stock in recorded for product SKU '{}' (ID: {}): +{} units (Ref: '{}')",
                product.getSku(), product.getId(), request.quantity(), request.reference());

        return inventoryMapper.toResponse(updatedInventory);
    }

    @Override
    @Transactional
    public InventoryResponse stockOut(StockOutRequest request) {
        Product product = findActiveProductOrThrow(request.productId());
        Inventory inventory = findInventoryOrThrow(product.getId());

        if (inventory.getQuantityAvailable() < request.quantity()) {
            throw new InsufficientStockException(String.format(
                    "Cannot remove %d units of %s. Available stock: %d",
                    request.quantity(),
                    product.getSku(),
                    inventory.getQuantityAvailable()
            ));
        }

        inventory.setQuantityAvailable(inventory.getQuantityAvailable() - request.quantity());
        Inventory updatedInventory = inventoryRepository.save(inventory);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .movementType(StockMovementType.STOCK_OUT)
                .quantity(request.quantity())
                .reference(request.reference())
                .reason(request.reason())
                .build();
        stockMovementRepository.save(movement);

        log.info("Stock out recorded for product SKU '{}' (ID: {}): -{} units (Ref: '{}')",
                product.getSku(), product.getId(), request.quantity(), request.reference());

        return inventoryMapper.toResponse(updatedInventory);
    }

    @Override
    @Transactional
    public InventoryResponse adjustStock(StockAdjustmentRequest request) {
        Product product = findActiveProductOrThrow(request.productId());
        Inventory inventory = findInventoryOrThrow(product.getId());

        if (request.newQuantity() < 0) {
            throw new InvalidStockAdjustmentException("New quantity cannot be negative: " + request.newQuantity());
        }

        int delta = request.newQuantity() - inventory.getQuantityAvailable();
        inventory.setQuantityAvailable(request.newQuantity());
        Inventory updatedInventory = inventoryRepository.save(inventory);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .movementType(StockMovementType.ADJUSTMENT)
                .quantity(delta)
                .reference(request.reference())
                .reason(request.reason())
                .build();
        stockMovementRepository.save(movement);

        log.info("Stock adjusted for product SKU '{}' (ID: {}): new quantity {} (delta: {}) (Ref: '{}')",
                product.getSku(), product.getId(), request.newQuantity(), delta, request.reference());

        return inventoryMapper.toResponse(updatedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {
        findProductOrThrow(productId);
        Inventory inventory = findInventoryOrThrow(productId);
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovements(Long productId) {
        findProductOrThrow(productId);
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(stockMovementMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockResponse> getLowStockInventory() {
        return inventoryRepository.findLowStockInventory().stream()
                .map(inventoryMapper::toLowStockResponse)
                .toList();
    }

    @Override
    @Transactional
    public Inventory initializeInventory(Product product) {
        return inventoryRepository.findByProductId(product.getId())
                .orElseGet(() -> {
                    Inventory inventory = Inventory.builder()
                            .product(product)
                            .quantityAvailable(0)
                            .reservedQuantity(0)
                            .build();
                    Inventory saved = inventoryRepository.save(inventory);
                    log.info("Initialized inventory record for product SKU '{}' (ID: {})", product.getSku(), product.getId());
                    return saved;
                });
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private Product findActiveProductOrThrow(Long productId) {
        Product product = findProductOrThrow(productId);
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessRuleException("Cannot perform inventory operations on inactive product: " + product.getSku());
        }
        return product;
    }

    private Inventory findInventoryOrThrow(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory record not found for product id: " + productId));
    }
}

