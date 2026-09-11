package com.factoryos.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Standardized API error envelope returned for all client and server errors")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "UTC timestamp when the error occurred", example = "2026-09-11T16:00:00Z")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Standard HTTP error reason phrase", example = "Bad Request")
        String error,

        @Schema(description = "Human-readable descriptive error message", example = "Validation failed for one or more fields")
        String message,

        @Schema(description = "Target API request URI path", example = "/api/products")
        String path,

        @Schema(description = "Map of field-level validation errors (field name -> violation message)")
        Map<String, String> errors,

        @Schema(description = "Alias map of validation errors for backward compatibility")
        Map<String, String> validationErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, null, null);
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> errors) {
        this(Instant.now(), status, error, message, path, errors, errors);
    }
}
