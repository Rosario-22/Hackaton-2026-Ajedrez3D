package com.hackaton2026.ajedrez3d.dto;

import java.util.UUID;

public record UserStatsResponse(
    UUID userId,
    String username,
    long gamesPlayed,
    long gamesWon,
    long gamesLost,
    long gamesDrawn
) {}