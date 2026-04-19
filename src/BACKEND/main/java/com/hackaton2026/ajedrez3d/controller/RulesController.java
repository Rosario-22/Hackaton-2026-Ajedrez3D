package com.hackaton2026.ajedrez3d.controller;

import com.hackaton2026.ajedrez3d.dto.RulesResponse;
import com.hackaton2026.ajedrez3d.model.BoardConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RulesController {

    @GetMapping("/rules")
    public RulesResponse rules() {
        return RulesResponse.defaultRules(BoardConstants.BOARD_SIZE);
    }
}
