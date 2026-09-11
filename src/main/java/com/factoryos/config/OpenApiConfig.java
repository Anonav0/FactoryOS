package com.factoryos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI factoryOsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FactoryOS Inventory Management API")
                        .description("REST API for managing industrial products, suppliers, inventory, stock movements, and purchase order lifecycles in a manufacturing environment.")
                        .version("1.0.0"))
                .tags(List.of(
                        new Tag().name("Products").description("Product catalog management, pricing, SKU uniqueness, and soft deactivation"),
                        new Tag().name("Suppliers").description("Supplier directory, contact information, and active status filtering"),
                        new Tag().name("Inventory").description("Real-time stock tracking, atomic stock-in/stock-out operations, adjustments, and low-stock monitoring"),
                        new Tag().name("Purchase Orders").description("Procurement workflow, line items, order numbering, and state machine transitions (CREATED -> APPROVED -> RECEIVED / CANCELLED)"),
                        new Tag().name("System").description("System health check and operational status")
                ));
    }
}
