package com.factoryos.controller;

import com.factoryos.dto.CreateProductRequest;
import com.factoryos.dto.ProductResponse;
import com.factoryos.dto.UpdateProductRequest;
import com.factoryos.exception.ErrorResponse;
import com.factoryos.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Validated
@Tag(name = "Products", description = "Product catalog management, pricing, SKU uniqueness, and soft deactivation")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new product",
            description = "Registers a new industrial product in the catalog. SKU must be globally unique. Automatically initializes an inventory record with 0 available stock."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product successfully created",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation constraints violated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate SKU already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(
            summary = "List all products",
            description = "Retrieves all products registered in the system (both active and deactivated)."
    )
    @ApiResponse(responseCode = "200", description = "List of all products successfully retrieved")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieves detailed information for a specific product by its internal database ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found and returned",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Product ID is non-positive or invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product with given ID does not exist",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Target product ID", example = "1")
            @PathVariable @Positive(message = "Product ID must be positive") Long id
    ) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/active")
    @Operation(
            summary = "List active products",
            description = "Retrieves all active products available for purchase orders, stock-in, and inventory operations."
    )
    @ApiResponse(responseCode = "200", description = "List of active products successfully retrieved")
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search products by name",
            description = "Performs a case-insensitive search across product names. Returns all products if name query parameter is blank or omitted."
    )
    @ApiResponse(responseCode = "200", description = "Matching products retrieved")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @Parameter(description = "Search term to match against product names", example = "Motor")
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(productService.searchProducts(name));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing product",
            description = "Updates product details such as name, description, category, unit price, and reorder level. SKU is immutable."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error on update payload or invalid ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product with given ID does not exist",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID of product to update", example = "1")
            @PathVariable @Positive(message = "Product ID must be positive") Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deactivate a product (soft delete)",
            description = "Sets the product active flag to false. Historical orders and stock movements remain intact, but future procurement and stock additions are blocked."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product with given ID does not exist",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deactivateProduct(
            @Parameter(description = "ID of product to deactivate", example = "1")
            @PathVariable @Positive(message = "Product ID must be positive") Long id
    ) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}
