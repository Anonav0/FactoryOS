package com.factoryos.controller;

import com.factoryos.dto.CreateSupplierRequest;
import com.factoryos.dto.SupplierResponse;
import com.factoryos.dto.UpdateSupplierRequest;
import com.factoryos.exception.ErrorResponse;
import com.factoryos.service.SupplierService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@Validated
@Tag(name = "Suppliers", description = "Supplier directory, contact information, and active status filtering")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @Operation(
            summary = "Onboard a new supplier",
            description = "Registers a new industrial supplier. Supplier email must be unique across all active/inactive supplier records."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supplier successfully onboarded",
                    content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error in request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Supplier with this email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse created = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(
            summary = "List all suppliers",
            description = "Retrieves all suppliers registered in the directory (both active and deactivated)."
    )
    @ApiResponse(responseCode = "200", description = "List of all suppliers successfully retrieved")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get supplier by ID",
            description = "Retrieves profile and contact details for a specific supplier by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier details retrieved",
                    content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Supplier ID is non-positive or invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier with given ID does not exist",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> getSupplierById(
            @Parameter(description = "Target supplier ID", example = "1")
            @PathVariable @Positive(message = "Supplier ID must be positive") Long id
    ) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @GetMapping("/active")
    @Operation(
            summary = "List active suppliers",
            description = "Retrieves all currently active suppliers eligible for purchase orders."
    )
    @ApiResponse(responseCode = "200", description = "List of active suppliers retrieved")
    public ResponseEntity<List<SupplierResponse>> getActiveSuppliers() {
        return ResponseEntity.ok(supplierService.getActiveSuppliers());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update supplier details",
            description = "Updates supplier profile information including contact person, email, phone, and address."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier updated successfully",
                    content = @Content(schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error in update body or invalid ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Updated email conflicts with another existing supplier",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SupplierResponse> updateSupplier(
            @Parameter(description = "Target supplier ID", example = "1")
            @PathVariable @Positive(message = "Supplier ID must be positive") Long id,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deactivate a supplier (soft delete)",
            description = "Marks a supplier inactive. Inactive suppliers cannot receive new purchase orders."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supplier successfully deactivated"),
            @ApiResponse(responseCode = "400", description = "Invalid supplier ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deactivateSupplier(
            @Parameter(description = "Target supplier ID", example = "1")
            @PathVariable @Positive(message = "Supplier ID must be positive") Long id
    ) {
        supplierService.deactivateSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
