package com.hackaton2026.ajedrez3d.dto;

import com.hackaton2026.ajedrez3d.model.Position;
import java.util.List;

public record LegalMovesResponse(
        Position from,
        String pieceType,
        List<Position> moves
) {
}
