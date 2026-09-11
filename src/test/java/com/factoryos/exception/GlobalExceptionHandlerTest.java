package com.factoryos.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Validated
    @RestController
    @RequestMapping("/test")
    static class TestController {

        record DummyDto(@NotBlank(message = "Field name cannot be blank") String name, @Min(value = 1, message = "Value must be positive") Integer value) {}

        @GetMapping("/resource-not-found")
        public void throwResourceNotFound() {
            throw new ResourceNotFoundException("Resource with ID 99 was not found");
        }

        @GetMapping("/inventory-not-found")
        public void throwInventoryNotFound() {
            throw new InventoryNotFoundException("Inventory record not found for product id: 99");
        }

        @GetMapping("/duplicate-resource")
        public void throwDuplicateResource() {
            throw new DuplicateResourceException("Product with SKU 'TEST-SKU' already exists");
        }

        @GetMapping("/insufficient-stock")
        public void throwInsufficientStock() {
            throw new InsufficientStockException("Insufficient stock for product SKU 'TEST-SKU'. Available: 5, Requested: 10");
        }

        @GetMapping("/invalid-po-state")
        public void throwInvalidPOState() {
            throw new InvalidPurchaseOrderStateException("Purchase order PO-000001 cannot be received because its current status is CREATED");
        }

        @GetMapping("/business-rule")
        public void throwBusinessRule() {
            throw new BusinessRuleException("Cannot add inactive product to purchase order: SKU-100");
        }

        @GetMapping("/invalid-stock-adjustment")
        public void throwInvalidStockAdjustment() {
            throw new InvalidStockAdjustmentException("New quantity must be greater than or equal to 0");
        }

        @GetMapping("/optimistic-lock")
        public void throwOptimisticLock() {
            throw new ObjectOptimisticLockingFailureException("Inventory", 1L);
        }

        @GetMapping("/data-integrity-unique")
        public void throwDataIntegrityUnique() {
            throw new DataIntegrityViolationException("duplicate key value violates unique constraint 'uk_products_sku'");
        }

        @GetMapping("/data-integrity-fk")
        public void throwDataIntegrityFK() {
            throw new DataIntegrityViolationException("violates foreign key constraint 'fk_purchase_orders_supplier'");
        }

        @GetMapping("/data-integrity-general")
        public void throwDataIntegrityGeneral() {
            throw new DataIntegrityViolationException("check constraint failed");
        }

        @PostMapping("/validation")
        public void testValidation(@Valid @RequestBody DummyDto dto) {}

        @GetMapping("/param-mismatch")
        public void testTypeMismatch(@RequestParam Long number) {}

        @GetMapping("/missing-param")
        public void testMissingParam(@RequestParam String requiredParam) {}

        @GetMapping("/unhandled-exception")
        public void throwUnhandledException() {
            throw new NullPointerException("Secret internal pointer error at internal.service.DBConnection");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("ResourceNotFoundException returns 404 with standard envelope")
    void testResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource with ID 99 was not found"))
                .andExpect(jsonPath("$.path").value("/test/resource-not-found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    @DisplayName("InventoryNotFoundException returns 404 with standard envelope")
    void testInventoryNotFoundException() throws Exception {
        mockMvc.perform(get("/test/inventory-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Inventory Not Found"))
                .andExpect(jsonPath("$.message").value("Inventory record not found for product id: 99"))
                .andExpect(jsonPath("$.path").value("/test/inventory-not-found"));
    }

    @Test
    @DisplayName("DuplicateResourceException returns 409 Conflict")
    void testDuplicateResourceException() throws Exception {
        mockMvc.perform(get("/test/duplicate-resource"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Product with SKU 'TEST-SKU' already exists"))
                .andExpect(jsonPath("$.path").value("/test/duplicate-resource"));
    }

    @Test
    @DisplayName("InsufficientStockException returns 409 Conflict")
    void testInsufficientStockException() throws Exception {
        mockMvc.perform(get("/test/insufficient-stock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Insufficient Stock"))
                .andExpect(jsonPath("$.message", containsString("Insufficient stock for product SKU 'TEST-SKU'")))
                .andExpect(jsonPath("$.path").value("/test/insufficient-stock"));
    }

    @Test
    @DisplayName("InvalidPurchaseOrderStateException returns 409 Conflict")
    void testInvalidPurchaseOrderStateException() throws Exception {
        mockMvc.perform(get("/test/invalid-po-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Invalid Purchase Order State"))
                .andExpect(jsonPath("$.message", containsString("cannot be received because its current status is CREATED")))
                .andExpect(jsonPath("$.path").value("/test/invalid-po-state"));
    }

    @Test
    @DisplayName("BusinessRuleException returns 400 Bad Request")
    void testBusinessRuleException() throws Exception {
        mockMvc.perform(get("/test/business-rule"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Cannot add inactive product to purchase order: SKU-100"))
                .andExpect(jsonPath("$.path").value("/test/business-rule"));
    }

    @Test
    @DisplayName("InvalidStockAdjustmentException returns 400 Bad Request")
    void testInvalidStockAdjustmentException() throws Exception {
        mockMvc.perform(get("/test/invalid-stock-adjustment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid Stock Adjustment"))
                .andExpect(jsonPath("$.message").value("New quantity must be greater than or equal to 0"));
    }

    @Test
    @DisplayName("ObjectOptimisticLockingFailureException returns 409 Conflict")
    void testOptimisticLockingException() throws Exception {
        mockMvc.perform(get("/test/optimistic-lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Optimistic Lock Conflict"))
                .andExpect(jsonPath("$.message", containsString("Please retry")));
    }

    @Test
    @DisplayName("DataIntegrityViolationException (unique) returns 409 with clean message")
    void testDataIntegrityUnique() throws Exception {
        mockMvc.perform(get("/test/data-integrity-unique"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("A resource with the specified unique identifier already exists"))
                .andExpect(jsonPath("$.message", not(containsString("uk_products_sku"))));
    }

    @Test
    @DisplayName("DataIntegrityViolationException (foreign key) returns 409 with clean message")
    void testDataIntegrityFK() throws Exception {
        mockMvc.perform(get("/test/data-integrity-fk"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cannot perform operation due to existing related records or invalid reference"))
                .andExpect(jsonPath("$.message", not(containsString("fk_purchase_orders_supplier"))));
    }

    @Test
    @DisplayName("DataIntegrityViolationException (general) returns 409 with clean message")
    void testDataIntegrityGeneral() throws Exception {
        mockMvc.perform(get("/test/data-integrity-general"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Database constraint violation occurred"));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException returns 400 with errors and validationErrors maps")
    void testMethodArgumentNotValid() throws Exception {
        TestController.DummyDto invalidDto = new TestController.DummyDto("", 0);

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.name").value("Field name cannot be blank"))
                .andExpect(jsonPath("$.errors.value").value("Value must be positive"))
                .andExpect(jsonPath("$.validationErrors.name").value("Field name cannot be blank"))
                .andExpect(jsonPath("$.validationErrors.value").value("Value must be positive"));
    }

    @Test
    @DisplayName("Malformed JSON returns 400 Bad Request with clean message")
    void testMalformedJson() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed Request"))
                .andExpect(jsonPath("$.message", containsString("Malformed JSON request body or invalid data type format")))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    @DisplayName("Type mismatch returns 400 Bad Request with clean message")
    void testTypeMismatch() throws Exception {
        mockMvc.perform(get("/test/param-mismatch?number=not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Type Mismatch"))
                .andExpect(jsonPath("$.message", containsString("Failed to convert value 'not-a-number' of parameter 'number' to required type")));
    }

    @Test
    @DisplayName("Missing request parameter returns 400 Bad Request")
    void testMissingParameter() throws Exception {
        mockMvc.perform(get("/test/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Missing Parameter"))
                .andExpect(jsonPath("$.message", containsString("Required request parameter 'requiredParam' is missing")));
    }

    @Test
    @DisplayName("Method not allowed returns 405 Method Not Allowed")
    void testMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/test/resource-not-found"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.message", containsString("HTTP method 'DELETE' is not supported for this endpoint")));
    }

    @Test
    @DisplayName("Unhandled Exception returns 500 without leaking stack traces or internal class names")
    void testUnhandledException() throws Exception {
        mockMvc.perform(get("/test/unhandled-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected internal error occurred"))
                .andExpect(jsonPath("$.message", not(containsString("NullPointerException"))))
                .andExpect(jsonPath("$.message", not(containsString("Secret internal pointer error"))))
                .andExpect(jsonPath("$.message", not(containsString("DBConnection"))))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }
}
