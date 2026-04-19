package com.hackaton2026.ajedrez3d.service;

import com.hackaton2026.ajedrez3d.model.Game;
import com.hackaton2026.ajedrez3d.model.GameStatus;
import com.hackaton2026.ajedrez3d.model.Piece;
import com.hackaton2026.ajedrez3d.model.PieceColor;
import com.hackaton2026.ajedrez3d.model.PieceType;
import com.hackaton2026.ajedrez3d.model.Position;
import org.springframework.stereotype.Component;

@Component
public class GameStateEvaluator {

    private final MoveCalculator moveCalculator;

    public GameStateEvaluator(MoveCalculator moveCalculator) {
        this.moveCalculator = moveCalculator;
    }

    public boolean isKingInCheck(Game game, PieceColor color) {
        Position kingPosition = null;
        for (Piece piece : game.allPieces()) {
            if (piece.getColor() == color && piece.getType() == PieceType.KING) {
                kingPosition = piece.getPosition();
                break;
            }
        }
        if (kingPosition == null) return false;
        return isSquareAttacked(game, kingPosition, color.opposite());
    }

    public boolean hasAnyLegalMoves(Game game, PieceColor color) {
        for (Piece piece : game.allPieces()) {
            if (piece.getColor() != color) continue;
            if (!moveCalculator.legalMoves(game, piece).isEmpty()) return true;
        }
        return false;
    }

    public boolean isCheckmate(Game game) {
        return game.getStatus() == GameStatus.IN_PROGRESS
                && isKingInCheck(game, game.getTurn())
                && !hasAnyLegalMoves(game, game.getTurn());
    }

    public boolean isStalemate(Game game) {
        return game.getStatus() == GameStatus.IN_PROGRESS
                && !isKingInCheck(game, game.getTurn())
                && !hasAnyLegalMoves(game, game.getTurn());
    }

    public void updateEndState(Game game) {
        if (isCheckmate(game)) {
            game.setStatus(game.getTurn() == PieceColor.WHITE ? GameStatus.BLACK_WON : GameStatus.WHITE_WON);
            game.setWinner(game.getTurn().opposite());
            return;
        }
        if (isStalemate(game)) {
            game.setStatus(GameStatus.DRAW);
        }
    }

    public String finishReason(Game game) {
        return switch (game.getStatus()) {
            case WHITE_WON, BLACK_WON -> "KING_CAPTURED_OR_CHECKMATE";
            case DRAW -> "STALEMATE";
            case IN_PROGRESS -> {
                if (isCheckmate(game)) yield "CHECKMATE";
                if (isStalemate(game)) yield "STALEMATE";
                if (isKingInCheck(game, PieceColor.WHITE) || isKingInCheck(game, PieceColor.BLACK)) yield "CHECK";
                yield "IN_PROGRESS";
            }
        };
    }

    private boolean isSquareAttacked(Game game, Position target, PieceColor attackerColor) {
        for (Piece piece : game.allPieces()) {
            if (piece.getColor() != attackerColor) continue;
            if (moveCalculator.pseudoMoves(game, piece).contains(target)) return true;
        }
        return false;
    }
}
