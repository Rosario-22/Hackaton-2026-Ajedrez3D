package com.hackaton2026.ajedrez3d.dto;

import com.hackaton2026.ajedrez3d.model.PieceColor;
import com.hackaton2026.ajedrez3d.model.PieceType;
import com.hackaton2026.ajedrez3d.model.Position;

public record InitialPlacementDto(
        PieceColor color,
        PieceType type,
        Position position
) {
}
