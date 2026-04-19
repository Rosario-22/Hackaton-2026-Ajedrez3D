package com.hackaton2026.ajedrez3d.service;

import com.hackaton2026.ajedrez3d.model.Game;
import com.hackaton2026.ajedrez3d.model.Piece;
import com.hackaton2026.ajedrez3d.model.PieceColor;
import com.hackaton2026.ajedrez3d.model.PieceType;
import com.hackaton2026.ajedrez3d.model.Position;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class MoveCalculator {

    List<Position> legalMoves(Game game, Piece piece) {
        Set<Position> candidates = pseudoMoves(game, piece);
        List<Position> legalMoves = new ArrayList<>();
        for (Position candidate : candidates) {
            if (!wouldLeaveKingInCheck(game, piece, candidate)) {
                legalMoves.add(candidate);
            }
        }
        legalMoves.sort(Comparator.comparing(Position::x).thenComparing(Position::y).thenComparing(Position::z));
        return legalMoves;
    }

    Set<Position> pseudoMoves(Game game, Piece piece) {
        return switch (piece.getType()) {
            case KING -> kingMoves(game, piece);
            case ROOK -> slideMoves(game, piece, new int[][]{
                    {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
            });
            case BISHOP -> slideMoves(game, piece, new int[][]{
                    {1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
                    {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
                    {0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1},
                    {1, 1, 1}, {1, 1, -1}, {1, -1, 1}, {1, -1, -1},
                    {-1, 1, 1}, {-1, 1, -1}, {-1, -1, 1}, {-1, -1, -1}
            });
            case UNICORN -> unicornMoves(game, piece);
            case KNIGHT -> knightMoves(game, piece);
        };
    }

    private Set<Position> kingMoves(Game game, Piece piece) {
        Set<Position> moves = new LinkedHashSet<>();
        Position from = piece.getPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    Position to = new Position(from.x() + dx, from.y() + dy, from.z() + dz);
                    if (!to.isInsideBoard()) continue;
                    Piece occupant = game.pieceAt(to).orElse(null);
                    if (occupant == null || occupant.getColor() != piece.getColor()) {
                        moves.add(to);
                    }
                }
            }
        }
        return moves;
    }

    private Set<Position> slideMoves(Game game, Piece piece, int[][] directions) {
        Set<Position> moves = new LinkedHashSet<>();
        Position from = piece.getPosition();
        for (int[] direction : directions) {
            int x = from.x() + direction[0];
            int y = from.y() + direction[1];
            int z = from.z() + direction[2];
            while (new Position(x, y, z).isInsideBoard()) {
                Position to = new Position(x, y, z);
                Piece occupant = game.pieceAt(to).orElse(null);
                if (occupant == null) {
                    moves.add(to);
                } else {
                    if (occupant.getColor() != piece.getColor()) moves.add(to);
                    break;
                }
                x += direction[0];
                y += direction[1];
                z += direction[2];
            }
        }
        return moves;
    }

    private Set<Position> knightMoves(Game game, Piece piece) {
        Set<Position> moves = new LinkedHashSet<>();
        Position from = piece.getPosition();
        int[][] offsets = {
                {2, 1, 0}, {2, -1, 0}, {-2, 1, 0}, {-2, -1, 0},
                {2, 0, 1}, {2, 0, -1}, {-2, 0, 1}, {-2, 0, -1},
                {1, 2, 0}, {1, -2, 0}, {-1, 2, 0}, {-1, -2, 0},
                {1, 0, 2}, {1, 0, -2}, {-1, 0, 2}, {-1, 0, -2},
                {0, 2, 1}, {0, 2, -1}, {0, -2, 1}, {0, -2, -1},
                {0, 1, 2}, {0, 1, -2}, {0, -1, 2}, {0, -1, -2}
        };
        for (int[] offset : offsets) {
            Position to = new Position(from.x() + offset[0], from.y() + offset[1], from.z() + offset[2]);
            if (!to.isInsideBoard()) continue;
            Piece occupant = game.pieceAt(to).orElse(null);
            if (occupant == null || occupant.getColor() != piece.getColor()) moves.add(to);
        }
        return moves;
    }

    private Set<Position> unicornMoves(Game game, Piece piece) {
        Set<Position> moves = new LinkedHashSet<>();
        Position from = piece.getPosition();
        int[][] offsets = {
                {1, 1, 1}, {1, 1, -1}, {1, -1, 1}, {1, -1, -1},
                {-1, 1, 1}, {-1, 1, -1}, {-1, -1, 1}, {-1, -1, -1}
        };
        for (int[] offset : offsets) {
            Position to = new Position(from.x() + offset[0], from.y() + offset[1], from.z() + offset[2]);
            if (!to.isInsideBoard()) continue;
            Piece occupant = game.pieceAt(to).orElse(null);
            if (occupant == null || occupant.getColor() != piece.getColor()) moves.add(to);
        }
        return moves;
    }

    private boolean wouldLeaveKingInCheck(Game game, Piece movingPiece, Position target) {
        Game simulation = cloneGame(game);
        Piece simulatedPiece = simulation.removePiece(movingPiece.getPosition()).orElseThrow();
        simulation.removePiece(target);
        simulatedPiece.setPosition(target);
        simulation.placePiece(simulatedPiece);
        return isKingInCheckInternal(simulation, movingPiece.getColor());
    }

    private boolean isKingInCheckInternal(Game game, PieceColor color) {
        Position kingPosition = null;
        for (Piece piece : game.allPieces()) {
            if (piece.getColor() == color && piece.getType() == PieceType.KING) {
                kingPosition = piece.getPosition();
                break;
            }
        }
        if (kingPosition == null) return false;
        for (Piece piece : game.allPieces()) {
            if (piece.getColor() != color && pseudoMoves(game, piece).contains(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    private Game cloneGame(Game source) {
        Game clone = new Game(source.getId());
        clone.setTurn(source.getTurn());
        clone.setStatus(source.getStatus());
        clone.setWinner(source.getWinner());
        clone.setLastMove(source.getLastMove());
        for (Piece piece : source.allPieces()) {
            clone.placePiece(new Piece(piece.getId(), piece.getType(), piece.getColor(), piece.getPosition()));
        }
        return clone;
    }
}
