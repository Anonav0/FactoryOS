package com.factoryos.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableConfigurationProperties(FactoryOsProperties.class)
public class CorsConfig implements WebMvcConfigurer {

    private final FactoryOsProperties factoryOsProperties;

    public CorsConfig(FactoryOsProperties factoryOsProperties) {
        this.factoryOsProperties = factoryOsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowedOrigins = factoryOsProperties.getCors().getAllowedOrigins();
        String[] originsArray = allowedOrigins.toArray(new String[0]);

        registry.addMapping("/api/**")
                .allowedOrigins(originsArray)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
