package com.factoryos.controller;

import com.factoryos.dto.InventoryResponse;
import com.factoryos.dto.LowStockResponse;
import com.factoryos.dto.StockAdjustmentRequest;
import com.factoryos.dto.StockInRequest;
import com.factoryos.dto.StockMovementResponse;
import com.factoryos.dto.StockOutRequest;
import com.factoryos.entity.StockMovementType;
import com.factoryos.exception.GlobalExceptionHandler;
import com.factoryos.exception.InsufficientStockException;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@Import(GlobalExceptionHandler.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void stockIn_validRequest_returns200() throws Exception {
        StockInRequest request = new StockInRequest(1L, 50, "GRN-1001", "Received from supplier");
        InventoryResponse response = new InventoryResponse(
                1L, 1L, "BRG-6204", "Steel Bearing 6204", 150, 0, false, Instant.now()
        );

        when(inventoryService.stockIn(any(StockInRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/inventory/stock-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityAvailable").value(150))
                .andExpect(jsonPath("$.sku").value("BRG-6204"));
    }

    @Test
    void stockOut_validRequest_returns200() throws Exception {
        StockOutRequest request = new StockOutRequest(1L, 20, "REQ-101", "Usage");
        InventoryResponse response = new InventoryResponse(
                1L, 1L, "BRG-6204", "Steel Bearing 6204", 80, 0, false, Instant.now()
        );

        when(inventoryService.stockOut(any(StockOutRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/inventory/stock-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityAvailable").value(80));
    }

    @Test
    void stockOut_insufficientStock_returns409() throws Exception {
        StockOutRequest request = new StockOutRequest(1L, 200, "REQ-102", "Usage");

        when(inventoryService.stockOut(any(StockOutRequest.class)))
                .thenThrow(new InsufficientStockException("Cannot remove 200 units of BRG-6204. Available stock: 80"));

        mockMvc.perform(post("/api/inventory/stock-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cannot remove 200 units of BRG-6204. Available stock: 80"));
    }

    @Test
    void adjustStock_validRequest_returns200() throws Exception {
        StockAdjustmentRequest request = new StockAdjustmentRequest(1L, 95, "AUDIT", "Count");
        InventoryResponse response = new InventoryResponse(
                1L, 1L, "BRG-6204", "Steel Bearing 6204", 95, 0, false, Instant.now()
        );

        when(inventoryService.adjustStock(any(StockAdjustmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/inventory/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityAvailable").value(95));
    }

    @Test
    void getAllInventory_returns200() throws Exception {
        InventoryResponse response = new InventoryResponse(
                1L, 1L, "BRG-6204", "Steel Bearing 6204", 95, 0, false, Instant.now()
        );

        when(inventoryService.getAllInventory()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void getInventoryByProductId_exists_returns200() throws Exception {
        InventoryResponse response = new InventoryResponse(
                1L, 1L, "BRG-6204", "Steel Bearing 6204", 95, 0, false, Instant.now()
        );

        when(inventoryService.getInventoryByProductId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("BRG-6204"));
    }

    @Test
    void getInventoryByProductId_notFound_returns404() throws Exception {
        when(inventoryService.getInventoryByProductId(999L))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/inventory/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getStockMovements_returns200() throws Exception {
        StockMovementResponse movement = new StockMovementResponse(
                1L, 1L, StockMovementType.STOCK_IN, 50, "REF-1", "Init", Instant.now()
        );

        when(inventoryService.getStockMovements(1L)).thenReturn(List.of(movement));

        mockMvc.perform(get("/api/inventory/1/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].movementType").value("STOCK_IN"));
    }

    @Test
    void getLowStockInventory_returns200() throws Exception {
        LowStockResponse lowStock = new LowStockResponse(
                1L, "BRG-6204", "Steel Bearing 6204", 15, 20, true
        );

        when(inventoryService.getLowStockInventory()).thenReturn(List.of(lowStock));

        mockMvc.perform(get("/api/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].lowStock").value(true));
    }
}

