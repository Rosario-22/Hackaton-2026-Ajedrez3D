package com.hackaton2026.ajedrez3d.dto;

import com.hackaton2026.ajedrez3d.model.Position;

public record PieceDto(
        String id,
        String type,
        String color,
        Position position
) {
}
