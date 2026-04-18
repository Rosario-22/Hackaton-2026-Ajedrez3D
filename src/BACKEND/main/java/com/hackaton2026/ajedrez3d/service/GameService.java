package com.hackaton2026.ajedrez3d.service;

import com.hackaton2026.ajedrez3d.dto.GameStateResponse;
import com.hackaton2026.ajedrez3d.dto.LegalMovesResponse;
import com.hackaton2026.ajedrez3d.dto.MoveRequest;
import com.hackaton2026.ajedrez3d.dto.MoveResponse;
import com.hackaton2026.ajedrez3d.dto.PieceDto;
import com.hackaton2026.ajedrez3d.model.Game;
import com.hackaton2026.ajedrez3d.model.GameStatus;
import com.hackaton2026.ajedrez3d.model.MoveSummary;
import com.hackaton2026.ajedrez3d.model.Piece;
import com.hackaton2026.ajedrez3d.model.PieceColor;
import com.hackaton2026.ajedrez3d.model.PieceType;
import com.hackaton2026.ajedrez3d.model.Position;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GameService {

    public static final int BOARD_SIZE = 5;

    private final Map<UUID, Game> games = new ConcurrentHashMap<>();

    public GameStateResponse createGame() {
        Game game = new Game(UUID.randomUUID());
        seedPieces(game);
        games.put(game.getId(), game);
        return toStateResponse(game);
    }

    public GameStateResponse getGameState(UUID id) {
        return toStateResponse(getGame(id));
    }

    public LegalMovesResponse getLegalMoves(UUID id, Position from) {
        Game game = getGame(id);
        Piece piece = getPiece(game, from);
        return new LegalMovesResponse(from, piece.getType().name(), legalMoves(game, piece));
    }

    public MoveResponse move(UUID id, MoveRequest request) {
        Game game = getGame(id);
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The game is already finished");
        }

        Piece movingPiece = getPiece(game, request.from());
        if (movingPiece.getColor() != game.getTurn()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It is not that piece's turn");
        }

        List<Position> legalMoves = legalMoves(game, movingPiece);
        if (!legalMoves.contains(request.to())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal move");
        }

        Piece capturedPiece = game.getPieces().remove(request.to());
        Position originalFrom = movingPiece.getPosition();
        game.getPieces().remove(originalFrom);
        movingPiece.setPosition(request.to());
        game.getPieces().put(request.to(), movingPiece);

        game.setLastMove(new MoveSummary(originalFrom, request.to(), movingPiece.getType(), movingPiece.getColor()));
        game.setTurn(game.getTurn().opposite());

        if (capturedPiece != null && capturedPiece.getType() == PieceType.KING) {
            game.setStatus(movingPiece.getColor() == PieceColor.WHITE ? GameStatus.WHITE_WON : GameStatus.BLACK_WON);
            game.setWinner(movingPiece.getColor());
        } else {
            updateEndState(game);
        }

        game.touch();

        return new MoveResponse(
                true,
                "Move applied",
                game.getId(),
                game.getTurn(),
                game.getStatus(),
                game.getWinner(),
                isKingInCheck(game, PieceColor.WHITE),
                isKingInCheck(game, PieceColor.BLACK),
                isCheckmate(game),
                isStalemate(game),
                finishReason(game),
                capturedPiece == null ? null : toDto(capturedPiece),
                game.getLastMove()
        );
    }

    private Game getGame(UUID id) {
        Game game = games.get(id);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return game;
    }

    private Piece getPiece(Game game, Position position) {
        if (position == null || !position.isInsideBoard()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position is outside the 5x5x5 board");
        }
        Piece piece = game.getPieces().get(position);
        if (piece == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No piece found at that position");
        }
        return piece;
    }

    private void seedPieces(Game game) {
        place(game, new Piece(PieceType.KING, PieceColor.WHITE, new Position(0, 0, 0)));
        place(game, new Piece(PieceType.ROOK, PieceColor.WHITE, new Position(1, 0, 0)));
        place(game, new Piece(PieceType.BISHOP, PieceColor.WHITE, new Position(0, 1, 0)));
        place(game, new Piece(PieceType.UNICORN, PieceColor.WHITE, new Position(1, 1, 1)));
        place(game, new Piece(PieceType.KNIGHT, PieceColor.WHITE, new Position(2, 0, 1)));

        place(game, new Piece(PieceType.KING, PieceColor.BLACK, new Position(4, 4, 4)));
        place(game, new Piece(PieceType.ROOK, PieceColor.BLACK, new Position(3, 4, 4)));
        place(game, new Piece(PieceType.BISHOP, PieceColor.BLACK, new Position(4, 3, 4)));
        place(game, new Piece(PieceType.UNICORN, PieceColor.BLACK, new Position(3, 3, 3)));
        place(game, new Piece(PieceType.KNIGHT, PieceColor.BLACK, new Position(2, 4, 3)));
    }

    private void place(Game game, Piece piece) {
        if (game.getPieces().containsKey(piece.getPosition())) {
            throw new IllegalStateException("Duplicate piece position at " + piece.getPosition());
        }
        game.getPieces().put(piece.getPosition(), piece);
    }

    private GameStateResponse toStateResponse(Game game) {
        List<PieceDto> pieces = game.getPieces().values().stream()
                .map(this::toDto)
                .sorted(Comparator
                        .comparing(PieceDto::color)
                        .thenComparing(PieceDto::type)
                        .thenComparing(dto -> dto.position().x())
                        .thenComparing(dto -> dto.position().y())
                        .thenComparing(dto -> dto.position().z()))
                .toList();

        return new GameStateResponse(
                game.getId(),
                BOARD_SIZE,
                game.getTurn(),
                game.getStatus(),
                game.getWinner(),
                isKingInCheck(game, PieceColor.WHITE),
                isKingInCheck(game, PieceColor.BLACK),
                hasAnyLegalMoves(game, PieceColor.WHITE),
                hasAnyLegalMoves(game, PieceColor.BLACK),
                isCheckmate(game),
                isStalemate(game),
                finishReason(game),
                pieces,
                game.getLastMove()
        );
    }

    private PieceDto toDto(Piece piece) {
        return new PieceDto(piece.getType(), piece.getColor(), piece.getPosition());
    }

    private List<Position> legalMoves(Game game, Piece piece) {
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

    private Set<Position> pseudoMoves(Game game, Piece piece) {
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
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    Position to = new Position(from.x() + dx, from.y() + dy, from.z() + dz);
                    if (!to.isInsideBoard()) {
                        continue;
                    }
                    Piece occupant = game.getPieces().get(to);
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
            while (isInsideBoard(x, y, z)) {
                Position to = new Position(x, y, z);
                Piece occupant = game.getPieces().get(to);
                if (occupant == null) {
                    moves.add(to);
                } else {
                    if (occupant.getColor() != piece.getColor()) {
                        moves.add(to);
                    }
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
            if (!to.isInsideBoard()) {
                continue;
            }
            Piece occupant = game.getPieces().get(to);
            if (occupant == null || occupant.getColor() != piece.getColor()) {
                moves.add(to);
            }
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
            if (!to.isInsideBoard()) {
                continue;
            }
            Piece occupant = game.getPieces().get(to);
            if (occupant == null || occupant.getColor() != piece.getColor()) {
                moves.add(to);
            }
        }
        return moves;
    }

    private boolean isInsideBoard(int x, int y, int z) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && z >= 0 && z < BOARD_SIZE;
    }

    private boolean wouldLeaveKingInCheck(Game game, Piece movingPiece, Position target) {
        Game simulation = cloneGame(game);
        Piece simulatedPiece = simulation.getPieces().remove(movingPiece.getPosition());
        simulation.getPieces().remove(target);
        simulatedPiece.setPosition(target);
        simulation.getPieces().put(target, simulatedPiece);
        return isKingInCheck(simulation, movingPiece.getColor());
    }

    private Game cloneGame(Game source) {
        Game clone = new Game(source.getId());
        clone.setTurn(source.getTurn());
        clone.setStatus(source.getStatus());
        clone.setWinner(source.getWinner());
        clone.setLastMove(source.getLastMove());
        for (Piece piece : source.getPieces().values()) {
            clone.getPieces().put(piece.getPosition(), new Piece(piece.getType(), piece.getColor(), piece.getPosition()));
        }
        return clone;
    }

    private boolean isKingInCheck(Game game, PieceColor color) {
        Position kingPosition = null;
        for (Piece piece : game.getPieces().values()) {
            if (piece.getColor() == color && piece.getType() == PieceType.KING) {
                kingPosition = piece.getPosition();
                break;
            }
        }
        if (kingPosition == null) {
            return false;
        }
        return isSquareAttacked(game, kingPosition, color.opposite());
    }

    private boolean isSquareAttacked(Game game, Position target, PieceColor attackerColor) {
        for (Piece piece : game.getPieces().values()) {
            if (piece.getColor() != attackerColor) {
                continue;
            }
            if (pseudoMoves(game, piece).contains(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyLegalMoves(Game game, PieceColor color) {
        for (Piece piece : game.getPieces().values()) {
            if (piece.getColor() != color) {
                continue;
            }
            if (!legalMoves(game, piece).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCheckmate(Game game) {
        return game.getStatus() == GameStatus.IN_PROGRESS
                && isKingInCheck(game, game.getTurn())
                && !hasAnyLegalMoves(game, game.getTurn());
    }

    private boolean isStalemate(Game game) {
        return game.getStatus() == GameStatus.IN_PROGRESS
                && !isKingInCheck(game, game.getTurn())
                && !hasAnyLegalMoves(game, game.getTurn());
    }

    private void updateEndState(Game game) {
        if (isCheckmate(game)) {
            game.setStatus(game.getTurn() == PieceColor.WHITE ? GameStatus.BLACK_WON : GameStatus.WHITE_WON);
            game.setWinner(game.getTurn().opposite());
            return;
        }
        if (isStalemate(game)) {
            game.setStatus(GameStatus.DRAW);
        }
    }

    private String finishReason(Game game) {
        return switch (game.getStatus()) {
            case WHITE_WON, BLACK_WON -> "KING_CAPTURED_OR_CHECKMATE";
            case DRAW -> "STALEMATE";
            case IN_PROGRESS -> {
                if (isCheckmate(game)) {
                    yield "CHECKMATE";
                }
                if (isStalemate(game)) {
                    yield "STALEMATE";
                }
                if (isKingInCheck(game, PieceColor.WHITE) || isKingInCheck(game, PieceColor.BLACK)) {
                    yield "CHECK";
                }
                yield "IN_PROGRESS";
            }
        };
    }
}
