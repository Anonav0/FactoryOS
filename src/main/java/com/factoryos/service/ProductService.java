package com.factoryos.service;

import com.factoryos.dto.CreateProductRequest;
import com.factoryos.dto.ProductResponse;
import com.factoryos.dto.UpdateProductRequest;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getActiveProducts();

    List<ProductResponse> searchProducts(String name);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    void deactivateProduct(Long id);
}

