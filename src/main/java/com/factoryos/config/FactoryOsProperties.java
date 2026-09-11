package com.factoryos.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "factoryos")
public class FactoryOsProperties {

    @Valid
    private final Cors cors = new Cors();

    public Cors getCors() {
        return cors;
    }

    public static class Cors {
        @NotEmpty(message = "Allowed origins must not be empty")
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }
}

