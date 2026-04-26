package com.hackaton2026.ajedrez3d.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameHistoryResponse(
    UUID gameId,
    String whitePlayer,
    String blackPlayer,
    String status,
    String winner,
    LocalDateTime createdAt,
    LocalDateTime finishedAt
) {}