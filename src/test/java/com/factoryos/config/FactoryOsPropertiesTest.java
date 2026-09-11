package com.factoryos.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class FactoryOsPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @EnableConfigurationProperties(FactoryOsProperties.class)
    static class TestConfig {}

    @Test
    @DisplayName("Should bind default allowed origins when no custom property is set")
    void shouldBindDefaultAllowedOrigins() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            FactoryOsProperties props = context.getBean(FactoryOsProperties.class);
            assertThat(props.getCors().getAllowedOrigins()).containsExactly("http://localhost:5173");
        });
    }

    @Test
    @DisplayName("Should bind custom comma-separated allowed origins")
    void shouldBindCustomAllowedOrigins() {
        contextRunner
                .withPropertyValues("factoryos.cors.allowed-origins=http://localhost:3000,https://app.factoryos.internal")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    FactoryOsProperties props = context.getBean(FactoryOsProperties.class);
                    assertThat(props.getCors().getAllowedOrigins())
                            .containsExactly("http://localhost:3000", "https://app.factoryos.internal");
                });
    }
}

