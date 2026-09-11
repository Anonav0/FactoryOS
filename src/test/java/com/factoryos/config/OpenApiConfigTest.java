package com.factoryos.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OpenAPI Specification: Exposes /v3/api-docs with FactoryOS metadata")
    void apiDocsEndpointReturnsMetadata() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("FactoryOS Inventory Management API"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.paths['/api/products']").exists())
                .andExpect(jsonPath("$.paths['/api/suppliers']").exists())
                .andExpect(jsonPath("$.paths['/api/inventory']").exists())
                .andExpect(jsonPath("$.paths['/api/purchase-orders']").exists())
                .andExpect(jsonPath("$.paths['/api/health']").exists());
    }

    @Test
    @DisplayName("Swagger UI: Exposes /swagger-ui/index.html endpoint")
    void swaggerUiEndpointIsAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().is3xxRedirection());
    }
}
