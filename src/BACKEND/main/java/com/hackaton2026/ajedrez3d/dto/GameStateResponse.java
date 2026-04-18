package com.hackaton2026.ajedrez3d.dto;

import com.hackaton2026.ajedrez3d.model.GameStatus;
import com.hackaton2026.ajedrez3d.model.MoveSummary;
import com.hackaton2026.ajedrez3d.model.PieceColor;
import java.util.List;
import java.util.UUID;

public record GameStateResponse(
        UUID gameId,
        int boardSize,
        PieceColor turn,
        GameStatus status,
        PieceColor winner,
        boolean whiteInCheck,
        boolean blackInCheck,
        boolean whiteHasMoves,
        boolean blackHasMoves,
        boolean checkmate,
        boolean stalemate,
        String finishReason,
        List<PieceDto> pieces,
        MoveSummary lastMove
) {
}
