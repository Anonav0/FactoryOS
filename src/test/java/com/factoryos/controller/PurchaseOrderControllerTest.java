package com.factoryos.controller;

import com.factoryos.dto.CreatePurchaseOrderRequest;
import com.factoryos.dto.PurchaseOrderItemRequest;
import com.factoryos.dto.PurchaseOrderItemResponse;
import com.factoryos.dto.PurchaseOrderResponse;
import com.factoryos.dto.PurchaseOrderSummaryResponse;
import com.factoryos.entity.PurchaseOrderStatus;
import com.factoryos.exception.BusinessRuleException;
import com.factoryos.exception.GlobalExceptionHandler;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.service.PurchaseOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurchaseOrderController.class)
@Import(GlobalExceptionHandler.class)
class PurchaseOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PurchaseOrderService purchaseOrderService;

    @Test
    @DisplayName("POST /api/purchase-orders returns 201 Created with created PO representation")
    void createPurchaseOrder_validRequest_returns201() throws Exception {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                1L,
                LocalDate.now().plusDays(10),
                List.of(new PurchaseOrderItemRequest(10L, 50, new BigDecimal("450.00")))
        );

        PurchaseOrderResponse response = new PurchaseOrderResponse(
                100L,
                "PO-000001",
                1L,
                "Apex Tools",
                PurchaseOrderStatus.CREATED,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("22500.00"),
                List.of(new PurchaseOrderItemResponse(
                        1L,
                        10L,
                        "BRG-6204",
                        "Steel Bearing",
                        50,
                        new BigDecimal("450.00"),
                        new BigDecimal("22500.00")
                )),
                Instant.now(),
                Instant.now()
        );

        when(purchaseOrderService.createPurchaseOrder(any(CreatePurchaseOrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.orderNumber").value("PO-000001"))
                .andExpect(jsonPath("$.supplierName").value("Apex Tools"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(22500.00))
                .andExpect(jsonPath("$.items[0].sku").value("BRG-6204"))
                .andExpect(jsonPath("$.items[0].subtotal").value(22500.00));
    }

    @Test
    @DisplayName("POST /api/purchase-orders returns 400 Bad Request when supplierId is null")
    void createPurchaseOrder_nullSupplier_returns400() throws Exception {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                null,
                LocalDate.now().plusDays(10),
                List.of(new PurchaseOrderItemRequest(10L, 50, new BigDecimal("450.00")))
        );

        mockMvc.perform(post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.supplierId").exists());
    }

    @Test
    @DisplayName("POST /api/purchase-orders returns 400 Bad Request when items list is empty")
    void createPurchaseOrder_emptyItems_returns400() throws Exception {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                1L,
                LocalDate.now().plusDays(10),
                Collections.emptyList()
        );

        mockMvc.perform(post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.items").exists());
    }

    @Test
    @DisplayName("POST /api/purchase-orders returns 400 when business rule is violated (e.g. duplicate product)")
    void createPurchaseOrder_businessRuleViolation_returns400() throws Exception {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                1L,
                LocalDate.now().plusDays(10),
                List.of(new PurchaseOrderItemRequest(10L, 50, new BigDecimal("450.00")))
        );

        when(purchaseOrderService.createPurchaseOrder(any(CreatePurchaseOrderRequest.class)))
                .thenThrow(new BusinessRuleException("Duplicate product found in purchase order items"));

        mockMvc.perform(post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate product found in purchase order items"));
    }

    @Test
    @DisplayName("GET /api/purchase-orders returns 200 OK with list of purchase order summaries")
    void getAllPurchaseOrders_returns200() throws Exception {
        PurchaseOrderSummaryResponse summary = new PurchaseOrderSummaryResponse(
                100L,
                "PO-000001",
                1L,
                "Apex Tools",
                PurchaseOrderStatus.CREATED,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                2,
                new BigDecimal("59025.00"),
                Instant.now()
        );

        when(purchaseOrderService.getAllPurchaseOrders()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/purchase-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("PO-000001"))
                .andExpect(jsonPath("$[0].supplierName").value("Apex Tools"))
                .andExpect(jsonPath("$[0].itemCount").value(2))
                .andExpect(jsonPath("$[0].totalAmount").value(59025.00));
    }

    @Test
    @DisplayName("GET /api/purchase-orders/{id} returns 200 OK with full details")
    void getPurchaseOrderById_exists_returns200() throws Exception {
        PurchaseOrderResponse response = new PurchaseOrderResponse(
                100L,
                "PO-000001",
                1L,
                "Apex Tools",
                PurchaseOrderStatus.CREATED,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("4500.00"),
                Collections.emptyList(),
                Instant.now(),
                Instant.now()
        );

        when(purchaseOrderService.getPurchaseOrderById(100L)).thenReturn(response);

        mockMvc.perform(get("/api/purchase-orders/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("PO-000001"))
                .andExpect(jsonPath("$.totalAmount").value(4500.00));
    }

    @Test
    @DisplayName("GET /api/purchase-orders/{id} returns 404 when purchase order not found")
    void getPurchaseOrderById_notFound_returns404() throws Exception {
        when(purchaseOrderService.getPurchaseOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Purchase order not found with id: 999"));

        mockMvc.perform(get("/api/purchase-orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Purchase order not found with id: 999"));
    }

    @Test
    @DisplayName("POST /api/purchase-orders/{id}/approve returns 200 OK with status APPROVED")
    void approvePurchaseOrder_returns200() throws Exception {
        PurchaseOrderResponse response = new PurchaseOrderResponse(
                100L,
                "PO-000001",
                1L,
                "Apex Tools",
                PurchaseOrderStatus.APPROVED,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("4500.00"),
                Collections.emptyList(),
                Instant.now(),
                Instant.now()
        );

        when(purchaseOrderService.approvePurchaseOrder(100L)).thenReturn(response);

        mockMvc.perform(post("/api/purchase-orders/100/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.orderNumber").value("PO-000001"));
    }

    @Test
    @DisplayName("POST /api/purchase-orders/{id}/receive returns 200 OK with status RECEIVED")
    void receivePurchaseOrder_returns200() throws Exception {
        PurchaseOrderResponse response = new PurchaseOrderResponse(
                100L,
                "PO-000001",
                1L,
                "Apex Tools",
                PurchaseOrderStatus.RECEIVED,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("4500.00"),
                Collections.emptyList(),
                Instant.now(),
                Instant.now()
        );

        when(purchaseOrderService.receivePurchaseOrder(100L)).thenReturn(response);

        mockMvc.perform(post("/api/purchase-orders/100/receive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.orderNumber").value("PO-000001"));
    }

    @Test
    @DisplayName("POST /api/purchase-orders/{id}/receive returns 409 Conflict when transition is invalid")
    void receivePurchaseOrder_invalidState_returns409() throws Exception {
        when(purchaseOrderService.receivePurchaseOrder(100L))
                .thenThrow(new com.factoryos.exception.InvalidPurchaseOrderStateException("Purchase order PO-000001 cannot be received because its current status is CREATED"));

        mockMvc.perform(post("/api/purchase-orders/100/receive"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Invalid Purchase Order State"))
                .andExpect(jsonPath("$.message").value("Purchase order PO-000001 cannot be received because its current status is CREATED"));
    }

    @Test
    @DisplayName("POST /api/purchase-orders/{id}/cancel returns 200 OK with status CANCELLED")
    void cancelPurchaseOrder_returns200() throws Exception {
        PurchaseOrderResponse response = new PurchaseOrderResponse(
                100L,
                "PO-000001",
                1L,
                "Apex Tools",
                PurchaseOrderStatus.CANCELLED,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("4500.00"),
                Collections.emptyList(),
                Instant.now(),
                Instant.now()
        );

        when(purchaseOrderService.cancelPurchaseOrder(100L)).thenReturn(response);

        mockMvc.perform(post("/api/purchase-orders/100/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.orderNumber").value("PO-000001"));
    }
}

