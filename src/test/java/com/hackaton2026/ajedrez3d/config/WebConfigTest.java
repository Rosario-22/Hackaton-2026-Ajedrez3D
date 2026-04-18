package com.hackaton2026.ajedrez3d.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class WebConfigTest {

    @Test
    void corsConfigurationCanBeRegistered() {
        assertDoesNotThrow(() -> new WebConfig().addCorsMappings(new CorsRegistry()));
    }
}
