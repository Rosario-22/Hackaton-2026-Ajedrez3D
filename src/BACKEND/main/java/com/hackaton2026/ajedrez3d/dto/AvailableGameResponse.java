package com.hackaton2026.ajedrez3d.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AvailableGameResponse(
    UUID gameId,
    String whitePlayer,
    LocalDateTime createdAt
) {}