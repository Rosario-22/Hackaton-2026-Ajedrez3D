package com.hackaton2026.ajedrez3d.dto;

import com.hackaton2026.ajedrez3d.model.GameStatus;
import com.hackaton2026.ajedrez3d.model.MoveSummary;
import com.hackaton2026.ajedrez3d.model.PieceColor;
import java.util.UUID;

public record MoveResponse(
        boolean valid,
        String message,
        UUID gameId,
        PieceColor turn,
        GameStatus status,
        PieceColor winner,
        boolean whiteInCheck,
        boolean blackInCheck,
        boolean checkmate,
        boolean stalemate,
        String finishReason,
        PieceDto capturedPiece,
        MoveSummary lastMove
) {
}
