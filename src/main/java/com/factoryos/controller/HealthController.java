package com.factoryos.controller;

import com.factoryos.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "System", description = "System health check and operational status")
public class HealthController {

    @GetMapping("/health")
    @Operation(
            summary = "Service health check",
            description = "Returns application operational status and service name to confirm service availability."
    )
    @ApiResponse(responseCode = "200", description = "Application is operational and healthy")
    public ResponseEntity<HealthResponse> getHealth() {
        return ResponseEntity.ok(new HealthResponse("UP", "FactoryOS"));
    }
}
