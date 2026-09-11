package com.factoryos.controller;

import com.factoryos.dto.CreatePurchaseOrderRequest;
import com.factoryos.dto.PurchaseOrderResponse;
import com.factoryos.dto.PurchaseOrderSummaryResponse;
import com.factoryos.exception.ErrorResponse;
import com.factoryos.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@Validated
@Tag(name = "Purchase Orders", description = "Procurement workflow, line items, order numbering, and state machine transitions (CREATED -> APPROVED -> RECEIVED / CANCELLED)")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new Purchase Order",
            description = "Creates a new multi-item purchase order in CREATED status. Enforces active supplier and active products. Duplicate products within the order are rejected. Financial totals (item subtotals and totalAmount) are authoritatively computed on the backend using BigDecimal. Creating a PO does not modify inventory balances."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Purchase Order successfully created with CREATED status",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed on payload or empty items collection",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Referenced supplier or product does not exist",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Supplier/Product is inactive or duplicate products found in line items",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderRequest request
    ) {
        PurchaseOrderResponse created = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(
            summary = "List all Purchase Orders",
            description = "Retrieves a summary list of all purchase orders ordered by creation date descending."
    )
    @ApiResponse(responseCode = "200", description = "Purchase orders retrieved successfully")
    public ResponseEntity<List<PurchaseOrderSummaryResponse>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Purchase Order by ID",
            description = "Retrieves full purchase order details including all associated product line items, prices, and subtotals."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase order found",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid PO ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Purchase order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(
            @Parameter(description = "Purchase Order ID", example = "1")
            @PathVariable @Positive(message = "Purchase Order ID must be positive") Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @GetMapping("/order-number/{orderNumber}")
    @Operation(
            summary = "Get Purchase Order by Order Number",
            description = "Looks up a purchase order by its unique sequential tracking number (e.g. PO-000001)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase order found",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Order number is blank",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Purchase order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderByOrderNumber(
            @Parameter(description = "Sequential order tracking code", example = "PO-000001")
            @PathVariable @NotBlank(message = "Order number cannot be blank") String orderNumber) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderByOrderNumber(orderNumber));
    }

    @PostMapping("/{id}/approve")
    @Operation(
            summary = "Approve a Purchase Order",
            description = "Transitions a purchase order from CREATED to APPROVED. Only orders in CREATED status may be approved. Orders in APPROVED, RECEIVED, or CANCELLED status cannot be approved."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase order successfully approved",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid PO ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Purchase order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invalid state transition (order is not in CREATED status)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PurchaseOrderResponse> approvePurchaseOrder(
            @Parameter(description = "ID of purchase order to approve", example = "1")
            @PathVariable @Positive(message = "Purchase Order ID must be positive") Long id) {
        return ResponseEntity.ok(purchaseOrderService.approvePurchaseOrder(id));
    }

    @PostMapping("/{id}/receive")
    @Operation(
            summary = "Receive goods for an approved Purchase Order",
            description = "Transitions a purchase order from APPROVED to RECEIVED. Executes inside an atomic @Transactional boundary: increments available stock for each line item, logs individual STOCK_IN movements, and updates the status to RECEIVED. If receiving any item fails, all changes roll back completely. Only APPROVED orders can be received."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase order goods received, inventory incremented, and movements created",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid PO ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Purchase order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Order is not in APPROVED state, or item product is inactive",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PurchaseOrderResponse> receivePurchaseOrder(
            @Parameter(description = "ID of purchase order to receive", example = "1")
            @PathVariable @Positive(message = "Purchase Order ID must be positive") Long id) {
        return ResponseEntity.ok(purchaseOrderService.receivePurchaseOrder(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel a Purchase Order",
            description = "Transitions a purchase order in CREATED or APPROVED status to CANCELLED. Orders that have already been RECEIVED or CANCELLED cannot be cancelled. Cancelling an order does not alter inventory or generate stock movements."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase order successfully cancelled",
                    content = @Content(schema = @Schema(implementation = PurchaseOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid PO ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Purchase order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Cannot cancel order because it is in a terminal state (RECEIVED or CANCELLED)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PurchaseOrderResponse> cancelPurchaseOrder(
            @Parameter(description = "ID of purchase order to cancel", example = "1")
            @PathVariable @Positive(message = "Purchase Order ID must be positive") Long id) {
        return ResponseEntity.ok(purchaseOrderService.cancelPurchaseOrder(id));
    }
}
