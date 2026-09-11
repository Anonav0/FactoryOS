package com.factoryos.config;

import com.factoryos.controller.HealthController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import(CorsConfig.class)
@TestPropertySource(properties = {
        "factoryos.cors.allowed-origins=http://localhost:5173,https://factoryos.production.example"
})
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should accept preflight CORS requests from configured development origin")
    void cors_shouldAllowConfiguredDevOrigin() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    @DisplayName("Should accept preflight CORS requests from configured production origin")
    void cors_shouldAllowConfiguredProdOrigin() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header(HttpHeaders.ORIGIN, "https://factoryos.production.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://factoryos.production.example"));
    }

    @Test
    @DisplayName("Should reject preflight CORS requests from unauthorized origins")
    void cors_shouldRejectUnconfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header(HttpHeaders.ORIGIN, "https://malicious-site.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}

