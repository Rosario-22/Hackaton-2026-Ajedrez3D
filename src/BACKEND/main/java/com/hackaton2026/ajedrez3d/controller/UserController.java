package com.hackaton2026.ajedrez3d.controller;

import com.hackaton2026.ajedrez3d.dto.GameHistoryResponse;
import com.hackaton2026.ajedrez3d.dto.UserStatsResponse;
import com.hackaton2026.ajedrez3d.service.UserStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserStatsService userStatsService;

    public UserController(UserStatsService userStatsService) {
        this.userStatsService = userStatsService;
    }

    @GetMapping("/{id}/stats")
    public UserStatsResponse getStats(@PathVariable UUID id) {
        return userStatsService.getStats(id);
    }

    @GetMapping("/{id}/games")
    public List<GameHistoryResponse> getGameHistory(@PathVariable UUID id) {
        return userStatsService.getGameHistory(id);
    }
}