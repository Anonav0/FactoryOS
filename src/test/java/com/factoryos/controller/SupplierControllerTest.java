package com.factoryos.controller;

import com.factoryos.dto.CreateSupplierRequest;
import com.factoryos.dto.SupplierResponse;
import com.factoryos.dto.UpdateSupplierRequest;
import com.factoryos.exception.GlobalExceptionHandler;
import com.factoryos.exception.ResourceNotFoundException;
import com.factoryos.service.SupplierService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierController.class)
@Import(GlobalExceptionHandler.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierService supplierService;

    @Test
    void createSupplier_validRequest_returns201() throws Exception {
        CreateSupplierRequest request = new CreateSupplierRequest(
                "ABC Industrial Supplies",
                "Rahul Sen",
                "contact@abcindustrial.example",
                "+91-9876543210",
                "Industrial Area, Kolkata",
                true
        );

        SupplierResponse response = new SupplierResponse(
                1L,
                "ABC Industrial Supplies",
                "Rahul Sen",
                "contact@abcindustrial.example",
                "+91-9876543210",
                "Industrial Area, Kolkata",
                true,
                Instant.now(),
                Instant.now()
        );

        when(supplierService.createSupplier(any(CreateSupplierRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("ABC Industrial Supplies"));
    }

    @Test
    void createSupplier_invalidEmail_returns400() throws Exception {
        CreateSupplierRequest request = new CreateSupplierRequest(
                "ABC Industrial Supplies",
                "Rahul Sen",
                "invalid-email-address",
                "+91-9876543210",
                "Industrial Area, Kolkata",
                true
        );

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void getSupplierById_exists_returns200() throws Exception {
        SupplierResponse response = new SupplierResponse(
                1L, "ABC Industrial", "Rahul", "abc@example.com", null, null, true, Instant.now(), Instant.now()
        );

        when(supplierService.getSupplierById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/suppliers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ABC Industrial"));
    }

    @Test
    void getSupplierById_notFound_returns404() throws Exception {
        when(supplierService.getSupplierById(999L))
                .thenThrow(new ResourceNotFoundException("Supplier not found with id: 999"));

        mockMvc.perform(get("/api/suppliers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAllSuppliers_returns200() throws Exception {
        SupplierResponse response = new SupplierResponse(
                1L, "ABC Industrial", "Rahul", "abc@example.com", null, null, true, Instant.now(), Instant.now()
        );

        when(supplierService.getAllSuppliers()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void updateSupplier_validRequest_returns200() throws Exception {
        UpdateSupplierRequest request = new UpdateSupplierRequest(
                "ABC Enterprises", null, "abc@example.com", null, null, true
        );

        SupplierResponse response = new SupplierResponse(
                1L, "ABC Enterprises", null, "abc@example.com", null, null, true, Instant.now(), Instant.now()
        );

        when(supplierService.updateSupplier(eq(1L), any(UpdateSupplierRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/suppliers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ABC Enterprises"));
    }

    @Test
    void deactivateSupplier_returns204() throws Exception {
        doNothing().when(supplierService).deactivateSupplier(1L);

        mockMvc.perform(delete("/api/suppliers/1"))
                .andExpect(status().isNoContent());
    }
}

