package com.factoryos.service;

import com.factoryos.dto.CreateProductRequest;
import com.factoryos.dto.ProductResponse;
import com.factoryos.dto.UpdateProductRequest;
import com.factoryos.entity.Product;
import com.factoryos.exception.DuplicateResourceException;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.mapper.ProductMapper;
import com.factoryos.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final InventoryService inventoryService;

    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductMapper productMapper,
            InventoryService inventoryService
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        String normalizedSku = request.sku().trim().toUpperCase();
        if (productRepository.existsBySku(normalizedSku)) {
            throw new DuplicateResourceException("Product with SKU '" + normalizedSku + "' already exists");
        }

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        inventoryService.initializeInventory(savedProduct);
        log.info("Created product SKU '{}' (ID: {}, name: '{}')", savedProduct.getSku(), savedProduct.getId(), savedProduct.getName());
        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findProductOrThrow(id);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String name) {
        if (name == null || name.isBlank()) {
            return getAllProducts();
        }
        return productRepository.findByNameContainingIgnoreCase(name.trim()).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = findProductOrThrow(id);

        // SKU is immutable and explicitly preserved
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setCategory(request.category().trim());
        product.setUnitPrice(request.unitPrice());
        product.setReorderLevel(request.reorderLevel());
        if (request.active() != null) {
            product.setActive(request.active());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Updated product ID {} (SKU: '{}')", updatedProduct.getId(), updatedProduct.getSku());
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = findProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Deactivated product ID {} (SKU: '{}')", product.getId(), product.getSku());
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }
}

