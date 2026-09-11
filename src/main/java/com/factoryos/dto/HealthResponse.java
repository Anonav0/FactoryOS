package com.factoryos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "System operational health status response")
public record HealthResponse(
        @Schema(description = "System status indicator", example = "UP")
        String status,

        @Schema(description = "Application name identifier", example = "FactoryOS")
        String application
) {}
