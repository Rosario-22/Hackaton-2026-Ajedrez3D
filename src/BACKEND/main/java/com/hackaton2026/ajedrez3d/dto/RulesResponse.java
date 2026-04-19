package com.hackaton2026.ajedrez3d.dto;

import com.hackaton2026.ajedrez3d.model.PieceColor;
import com.hackaton2026.ajedrez3d.model.PieceType;
import com.hackaton2026.ajedrez3d.model.Position;
import java.util.List;

public record RulesResponse(
        int boardSize,
        List<PieceRuleDto> pieces,
        List<InitialPlacementDto> whiteSetup,
        List<InitialPlacementDto> blackSetup,
        String winCondition,
        String drawCondition
) {
    public static RulesResponse defaultRules(int boardSize) {
        List<PieceRuleDto> pieces = List.of(
                new PieceRuleDto(
                        PieceType.KING,
                        "Pieza central. Define la derrota si es capturada o queda sin jugadas.",
                        List.of("1 casilla en cualquier direccion", "ortogonal", "diagonal plana", "diagonal 3D")
                ),
                new PieceRuleDto(
                        PieceType.ROOK,
                        "Control ortogonal de lineas rectas.",
                        List.of("Cualquier cantidad de casillas", "solo un eje por vez")
                ),
                new PieceRuleDto(
                        PieceType.BISHOP,
                        "Control diagonal de planos y 3D.",
                        List.of("Cualquier cantidad de casillas", "dos o tres ejes a la vez")
                ),
                new PieceRuleDto(
                        PieceType.UNICORN,
                        "Salto diagonal 3D.",
                        List.of("1 casilla", "tres ejes a la vez")
                ),
                new PieceRuleDto(
                        PieceType.KNIGHT,
                        "Pieza de salto tactico.",
                        List.of("Movimiento en L 2D y 3D", "2 casillas en un eje y 1 en otro", "puede saltar piezas")
                )
        );

        List<InitialPlacementDto> whiteSetup = List.of(
                new InitialPlacementDto(PieceColor.WHITE, PieceType.KING, new Position(0, 0, 0)),
                new InitialPlacementDto(PieceColor.WHITE, PieceType.ROOK, new Position(1, 0, 0)),
                new InitialPlacementDto(PieceColor.WHITE, PieceType.BISHOP, new Position(0, 1, 0)),
                new InitialPlacementDto(PieceColor.WHITE, PieceType.UNICORN, new Position(1, 1, 1)),
                new InitialPlacementDto(PieceColor.WHITE, PieceType.KNIGHT, new Position(1, 1, 0))
        );

        List<InitialPlacementDto> blackSetup = List.of(
                new InitialPlacementDto(PieceColor.BLACK, PieceType.KING, new Position(boardSize - 4, boardSize - 4, boardSize - 4)),
                new InitialPlacementDto(PieceColor.BLACK, PieceType.ROOK, new Position(boardSize - 3, boardSize - 4, boardSize - 4)),
                new InitialPlacementDto(PieceColor.BLACK, PieceType.BISHOP, new Position(boardSize - 1, boardSize - 2, boardSize - 1)),
                new InitialPlacementDto(PieceColor.BLACK, PieceType.UNICORN, new Position(boardSize - 2, boardSize - 2, boardSize - 2)),
                new InitialPlacementDto(PieceColor.BLACK, PieceType.KNIGHT, new Position(boardSize - 3, boardSize - 1, boardSize - 2))
        );

        return new RulesResponse(
                boardSize,
                pieces,
                whiteSetup,
                blackSetup,
                "Gana el jugador que capture al rey rival o fuerce jaque mate.",
                "Empate por ahogado si el jugador al turno no tiene jugadas legales y su rey no esta en jaque."
        );
    }
}
