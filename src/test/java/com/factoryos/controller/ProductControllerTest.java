package com.factoryos.controller;

import com.factoryos.dto.CreateProductRequest;
import com.factoryos.dto.ProductResponse;
import com.factoryos.dto.UpdateProductRequest;
import com.factoryos.exception.DuplicateResourceException;
import com.factoryos.exception.GlobalExceptionHandler;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void createProduct_validRequest_returns201() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "BRG-6204",
                "Steel Bearing 6204",
                "Industrial bearing",
                "Bearings",
                new BigDecimal("450.00"),
                20,
                true
        );

        ProductResponse response = new ProductResponse(
                1L,
                "BRG-6204",
                "Steel Bearing 6204",
                "Industrial bearing",
                "Bearings",
                new BigDecimal("450.00"),
                20,
                true,
                Instant.now(),
                Instant.now()
        );

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.sku").value("BRG-6204"))
                .andExpect(jsonPath("$.name").value("Steel Bearing 6204"));
    }

    @Test
    void createProduct_duplicateSku_returns409() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "BRG-6204",
                "Steel Bearing 6204",
                "Industrial bearing",
                "Bearings",
                new BigDecimal("450.00"),
                20,
                true
        );

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new DuplicateResourceException("Product with SKU 'BRG-6204' already exists"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Product with SKU 'BRG-6204' already exists"));
    }

    @Test
    void createProduct_invalidInput_returns400() throws Exception {
        CreateProductRequest invalidRequest = new CreateProductRequest(
                "", // Blank SKU
                "", // Blank name
                null,
                "", // Blank category
                new BigDecimal("-10.00"), // Negative price
                -5, // Negative reorder level
                true
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.sku").exists())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.unitPrice").exists())
                .andExpect(jsonPath("$.validationErrors.reorderLevel").exists());
    }

    @Test
    void getProductById_exists_returns200() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "BRG-6204", "Steel Bearing", null, "Bearings",
                new BigDecimal("450.00"), 20, true, Instant.now(), Instant.now()
        );

        when(productService.getProductById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.sku").value("BRG-6204"));
    }

    @Test
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }

    @Test
    void getAllProducts_returns200() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "BRG-6204", "Steel Bearing", null, "Bearings",
                new BigDecimal("450.00"), 20, true, Instant.now(), Instant.now()
        );

        when(productService.getAllProducts()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void updateProduct_validRequest_returns200() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Bearing", "Desc", "Bearings",
                new BigDecimal("500.00"), 30, true
        );

        ProductResponse response = new ProductResponse(
                1L, "BRG-6204", "Updated Bearing", "Desc", "Bearings",
                new BigDecimal("500.00"), 30, true, Instant.now(), Instant.now()
        );

        when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Bearing"));
    }

    @Test
    void deactivateProduct_returns204() throws Exception {
        doNothing().when(productService).deactivateProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}

