package com.factoryos.service;

import com.factoryos.dto.CreateProductRequest;
import com.factoryos.dto.ProductResponse;
import com.factoryos.dto.UpdateProductRequest;
import com.factoryos.entity.Product;
import com.factoryos.exception.DuplicateResourceException;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.mapper.ProductMapper;
import com.factoryos.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @Spy
    private ProductMapper productMapper = new ProductMapper();

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

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
    }

    @Test
    void createProduct_success() {
        CreateProductRequest request = new CreateProductRequest(
                "BRG-6204",
                "Steel Bearing 6204",
                "Industrial deep-groove bearing",
                "Bearings",
                new BigDecimal("450.00"),
                20,
                true
        );

        when(productRepository.existsBySku("BRG-6204")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull();
        assertThat(response.sku()).isEqualTo("BRG-6204");
        verify(productRepository).save(any(Product.class));
        verify(inventoryService).initializeInventory(any(Product.class));
    }

    @Test
    void createProduct_duplicateSku_throwsDuplicateResourceException() {
        CreateProductRequest request = new CreateProductRequest(
                "brg-6204",
                "Steel Bearing 6204",
                null,
                "Bearings",
                new BigDecimal("450.00"),
                20,
                true
        );

        when(productRepository.existsBySku("BRG-6204")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("BRG-6204");
    }

    @Test
    void getProductById_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Steel Bearing 6204");
    }

    @Test
    void getProductById_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateProduct_success_preservesSku() {
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Bearing 6204",
                "Updated description",
                "Mechanical",
                new BigDecimal("520.00"),
                25,
                true
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse response = productService.updateProduct(1L, request);

        assertThat(response).isNotNull();
        assertThat(testProduct.getName()).isEqualTo("Updated Bearing 6204");
        assertThat(testProduct.getSku()).isEqualTo("BRG-6204"); // SKU preserved
        assertThat(testProduct.getUnitPrice()).isEqualTo(new BigDecimal("520.00"));
        verify(productRepository).save(testProduct);
    }

    @Test
    void deactivateProduct_success_setsActiveFalse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        productService.deactivateProduct(1L);

        assertThat(testProduct.getActive()).isFalse();
        verify(productRepository).save(testProduct);
    }

    @Test
    void searchProducts_success() {
        when(productRepository.findByNameContainingIgnoreCase("bearing"))
                .thenReturn(List.of(testProduct));

        List<ProductResponse> results = productService.searchProducts("bearing");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).name()).isEqualTo("Steel Bearing 6204");
    }
}

