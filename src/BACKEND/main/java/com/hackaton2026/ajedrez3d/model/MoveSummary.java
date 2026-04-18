package com.hackaton2026.ajedrez3d.model;

public record MoveSummary(
        Position from,
        Position to,
        PieceType pieceType,
        PieceColor color
) {
}
