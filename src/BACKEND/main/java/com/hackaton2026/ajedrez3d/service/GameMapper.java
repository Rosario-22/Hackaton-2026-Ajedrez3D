package com.hackaton2026.ajedrez3d.service;

import com.hackaton2026.ajedrez3d.dto.GameStateResponse;
import com.hackaton2026.ajedrez3d.dto.PieceDto;
import com.hackaton2026.ajedrez3d.model.Game;
import com.hackaton2026.ajedrez3d.model.Piece;
import com.hackaton2026.ajedrez3d.model.PieceColor;
import com.hackaton2026.ajedrez3d.model.PieceType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {

    private final GameStateEvaluator evaluator;

    public GameMapper(GameStateEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public GameStateResponse toStateResponse(Game game) {
        List<PieceDto> pieces = game.allPieces().stream()
                .map(this::toDto)
                .toList();

        return new GameStateResponse(
                game.getId(),
                com.hackaton2026.ajedrez3d.model.BoardConstants.BOARD_SIZE,
                game.getTurn(),
                game.getStatus(),
                game.getWinner(),
                evaluator.isKingInCheck(game, PieceColor.WHITE),
                evaluator.isKingInCheck(game, PieceColor.BLACK),
                evaluator.hasAnyLegalMoves(game, PieceColor.WHITE),
                evaluator.hasAnyLegalMoves(game, PieceColor.BLACK),
                evaluator.isCheckmate(game),
                evaluator.isStalemate(game),
                evaluator.finishReason(game),
                pieces,
                game.getLastMove()
        );
    }

    public PieceDto toDto(Piece piece) {
        return new PieceDto(
                piece.getId(),
                toPieceTypeName(piece.getType()),
                piece.getColor().name().toLowerCase(),
                piece.getPosition()
        );
    }

    private String toPieceTypeName(PieceType type) {
        return switch (type) {
            case KING -> "rey";
            case ROOK -> "torre";
            case BISHOP -> "alfil";
            case UNICORN -> "unicornio";
            case KNIGHT -> "caballo";
        };
    }
}
