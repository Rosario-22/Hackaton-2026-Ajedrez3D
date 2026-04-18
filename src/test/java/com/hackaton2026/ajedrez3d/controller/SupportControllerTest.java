package com.hackaton2026.ajedrez3d.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.hackaton2026.ajedrez3d.dto.RulesResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SupportControllerTest {

    private final HealthController healthController = new HealthController();
    private final RulesController rulesController = new RulesController();

    @Test
    void healthEndpointReturnsUp() {
        Map<String, String> health = healthController.health();

        assertEquals("UP", health.get("status"));
    }

    @Test
    void rulesEndpointReturnsDefaultRules() {
        RulesResponse rules = rulesController.rules();

        assertEquals(5, rules.boardSize());
        assertEquals(5, rules.pieces().size());
        assertEquals(5, rules.whiteSetup().size());
        assertEquals(5, rules.blackSetup().size());
        assertNotNull(rules.winCondition());
        assertNotNull(rules.drawCondition());
    }
}
