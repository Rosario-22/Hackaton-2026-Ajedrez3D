package com.hackaton2026.ajedrez3d.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hackaton2026.ajedrez3d.dto.GameStateResponse;
import com.hackaton2026.ajedrez3d.dto.LegalMovesResponse;
import com.hackaton2026.ajedrez3d.dto.MoveRequest;
import com.hackaton2026.ajedrez3d.dto.MoveResponse;
import com.hackaton2026.ajedrez3d.model.Game;
import com.hackaton2026.ajedrez3d.model.GameStatus;
import com.hackaton2026.ajedrez3d.model.Piece;
import com.hackaton2026.ajedrez3d.model.PieceColor;
import com.hackaton2026.ajedrez3d.model.PieceType;
import com.hackaton2026.ajedrez3d.model.Position;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class GameServiceTest {

    private GameService service;

    @BeforeEach
    void setUp() {
        service = new GameService();
    }

    @Test
    void createGameSeedsTheInitialBoard() {
        GameStateResponse state = service.createGame();

        assertEquals(5, state.boardSize());
        assertEquals(PieceColor.WHITE, state.turn());
        assertEquals(GameStatus.IN_PROGRESS, state.status());
        assertNull(state.winner());
        assertFalse(state.whiteInCheck());
        assertFalse(state.blackInCheck());
        assertTrue(state.whiteHasMoves());
        assertTrue(state.blackHasMoves());
        assertFalse(state.checkmate());
        assertFalse(state.stalemate());
        assertEquals("IN_PROGRESS", state.finishReason());
        assertEquals(10, state.pieces().size());
        assertTrue(containsPiece(state, PieceColor.WHITE, PieceType.KING, 0, 0, 0));
        assertTrue(containsPiece(state, PieceColor.BLACK, PieceType.KING, 4, 4, 4));
    }

    @Test
    void getGameStateThrowsWhenGameDoesNotExist() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getGameState(UUID.randomUUID()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getLegalMovesReturnsKingNeighborsInTheCenter() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 2, 2, 2);
            put(game, PieceType.KING, PieceColor.BLACK, 4, 4, 4);
        });

        LegalMovesResponse moves = service.getLegalMoves(gameId, new Position(2, 2, 2));

        assertEquals("KING", moves.pieceType());
        assertEquals(26, moves.moves().size());
        assertTrue(moves.moves().contains(new Position(3, 2, 2)));
        assertTrue(moves.moves().contains(new Position(3, 3, 3)));
    }

    @Test
    void getLegalMovesReturnsRookMovesAndStopsAtBlockingPieces() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 0, 0, 0);
            put(game, PieceType.ROOK, PieceColor.WHITE, 2, 2, 2);
            put(game, PieceType.BISHOP, PieceColor.WHITE, 4, 2, 2);
            put(game, PieceType.KNIGHT, PieceColor.BLACK, 2, 4, 2);
            put(game, PieceType.KING, PieceColor.BLACK, 4, 4, 4);
        });

        LegalMovesResponse moves = service.getLegalMoves(gameId, new Position(2, 2, 2));

        assertEquals("ROOK", moves.pieceType());
        assertTrue(moves.moves().contains(new Position(3, 2, 2)));
        assertTrue(moves.moves().contains(new Position(2, 4, 2)));
        assertFalse(moves.moves().contains(new Position(4, 2, 2)));
    }

    @Test
    void getLegalMovesReturnsBishopMoves() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 0, 0, 0);
            put(game, PieceType.BISHOP, PieceColor.WHITE, 2, 2, 2);
            put(game, PieceType.ROOK, PieceColor.WHITE, 4, 4, 2);
            put(game, PieceType.KNIGHT, PieceColor.BLACK, 0, 0, 2);
            put(game, PieceType.KING, PieceColor.BLACK, 4, 4, 4);
        });

        LegalMovesResponse moves = service.getLegalMoves(gameId, new Position(2, 2, 2));

        assertEquals("BISHOP", moves.pieceType());
        assertTrue(moves.moves().contains(new Position(3, 3, 2)));
        assertTrue(moves.moves().contains(new Position(0, 0, 2)));
        assertFalse(moves.moves().contains(new Position(4, 4, 2)));
    }

    @Test
    void getLegalMovesReturnsUnicornMoves() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 0, 0, 0);
            put(game, PieceType.UNICORN, PieceColor.WHITE, 2, 2, 2);
            put(game, PieceType.KING, PieceColor.BLACK, 4, 4, 4);
        });

        LegalMovesResponse moves = service.getLegalMoves(gameId, new Position(2, 2, 2));

        assertEquals("UNICORN", moves.pieceType());
        assertTrue(moves.moves().contains(new Position(3, 3, 3)));
        assertTrue(moves.moves().contains(new Position(1, 1, 1)));
        assertTrue(moves.moves().contains(new Position(4, 4, 4)));
    }

    @Test
    void getLegalMovesReturnsKnightMoves() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 0, 0, 0);
            put(game, PieceType.KNIGHT, PieceColor.WHITE, 2, 2, 2);
            put(game, PieceType.KING, PieceColor.BLACK, 4, 4, 4);
        });

        LegalMovesResponse moves = service.getLegalMoves(gameId, new Position(2, 2, 2));

        assertEquals("KNIGHT", moves.pieceType());
        assertEquals(24, moves.moves().size());
        assertTrue(moves.moves().contains(new Position(4, 3, 2)));
        assertTrue(moves.moves().contains(new Position(1, 0, 2)));
    }

    @Test
    void moveAppliesTheMoveAndFlipsTheTurn() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 0, 0, 0);
            put(game, PieceType.ROOK, PieceColor.WHITE, 2, 2, 2);
            put(game, PieceType.KING, PieceColor.BLACK, 4, 4, 4);
        });

        MoveResponse response = service.move(gameId, new MoveRequest(new Position(2, 2, 2), new Position(3, 2, 2)));

        assertTrue(response.valid());
        assertEquals(PieceColor.BLACK, response.turn());
        assertEquals(GameStatus.IN_PROGRESS, response.status());
        assertNull(response.winner());
        assertNotNull(response.lastMove());
        assertEquals(new Position(2, 2, 2), response.lastMove().from());
        assertEquals(new Position(3, 2, 2), response.lastMove().to());
        assertNull(response.capturedPiece());
        assertTrue(getGame(gameId).getPieces().containsKey(new Position(3, 2, 2)));
        assertFalse(getGame(gameId).getPieces().containsKey(new Position(2, 2, 2)));
    }

    @Test
    void moveRejectsAnIllegalMoveThatWouldExposeTheKing() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 0, 0, 0);
            put(game, PieceType.ROOK, PieceColor.WHITE, 1, 0, 0);
            put(game, PieceType.ROOK, PieceColor.BLACK, 4, 0, 0);
            put(game, PieceType.KING, PieceColor.BLACK, 4, 4, 4);
        });

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.move(gameId, new MoveRequest(new Position(1, 0, 0), new Position(1, 1, 0))));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Illegal move"));
    }

    @Test
    void moveRejectsWhenItIsNotThePieceTurn() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 0, 0, 0);
            put(game, PieceType.KING, PieceColor.BLACK, 4, 4, 4);
            put(game, PieceType.ROOK, PieceColor.BLACK, 4, 0, 0);
        });

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.move(gameId, new MoveRequest(new Position(4, 0, 0), new Position(3, 0, 0))));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("turn"));
    }

    @Test
    void moveCapturesTheKingAndEndsTheGame() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 0, 4, 4);
            put(game, PieceType.ROOK, PieceColor.WHITE, 1, 0, 0);
            put(game, PieceType.KING, PieceColor.BLACK, 4, 0, 0);
        });

        MoveResponse response = service.move(gameId, new MoveRequest(new Position(1, 0, 0), new Position(4, 0, 0)));

        assertTrue(response.valid());
        assertEquals(GameStatus.WHITE_WON, response.status());
        assertEquals(PieceColor.WHITE, response.winner());
        assertEquals("KING_CAPTURED_OR_CHECKMATE", response.finishReason());
        assertNotNull(response.capturedPiece());
        assertEquals(PieceType.KING, response.capturedPiece().type());
        assertThrows(ResponseStatusException.class,
                () -> service.move(gameId, new MoveRequest(new Position(0, 4, 4), new Position(0, 3, 4))));
    }

    @Test
    void getGameStateReportsCheckmateAndFinishReason() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 4, 4, 4);
            put(game, PieceType.KING, PieceColor.BLACK, 0, 0, 0);
            put(game, PieceType.ROOK, PieceColor.WHITE, 1, 0, 0);
            put(game, PieceType.ROOK, PieceColor.WHITE, 0, 1, 0);
            put(game, PieceType.ROOK, PieceColor.WHITE, 0, 0, 1);
            put(game, PieceType.UNICORN, PieceColor.WHITE, 2, 2, 2);
            game.setTurn(PieceColor.BLACK);
        });

        GameStateResponse updated = service.getGameState(gameId);

        assertTrue(updated.blackInCheck());
        assertFalse(updated.whiteInCheck());
        assertFalse(updated.blackHasMoves());
        assertTrue(updated.checkmate());
        assertEquals("CHECKMATE", updated.finishReason());
    }

    @Test
    void getGameStateReportsStalemateWhenThePlayerHasNoPiecesAndNoKing() {
        GameStateResponse state = service.createGame();
        UUID gameId = state.gameId();

        withCustomBoard(gameId, game -> {
            game.getPieces().clear();
            put(game, PieceType.KING, PieceColor.WHITE, 0, 0, 0);
            game.setTurn(PieceColor.BLACK);
        });

        GameStateResponse updated = service.getGameState(gameId);

        assertFalse(updated.blackInCheck());
        assertFalse(updated.blackHasMoves());
        assertTrue(updated.stalemate());
        assertEquals("STALEMATE", updated.finishReason());
    }

    @Test
    void getLegalMovesRejectsPositionsOutsideTheBoard() {
        GameStateResponse state = service.createGame();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getLegalMoves(state.gameId(), new Position(5, 0, 0)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void getPieceThrowsWhenNoPieceIsPresentAtTheRequestedSquare() {
        GameStateResponse state = service.createGame();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getLegalMoves(state.gameId(), new Position(4, 0, 4)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void positionHelperReportsBoardBounds() {
        assertTrue(new Position(0, 0, 0).isInsideBoard());
        assertTrue(new Position(4, 4, 4).isInsideBoard());
        assertFalse(new Position(5, 0, 0).isInsideBoard());
    }

    @Test
    void gameModelTracksTimestampsAndStatus() {
        Game game = new Game(UUID.randomUUID());

        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertNotNull(game.getCreatedAt());
        assertNotNull(game.getUpdatedAt());
        game.setTurn(PieceColor.BLACK);
        game.setStatus(GameStatus.DRAW);
        game.setWinner(PieceColor.WHITE);
        game.touch();

        assertEquals(PieceColor.BLACK, game.getTurn());
        assertEquals(GameStatus.DRAW, game.getStatus());
        assertEquals(PieceColor.WHITE, game.getWinner());
        assertDoesNotThrow(game::touch);
    }

    private boolean containsPiece(GameStateResponse state, PieceColor color, PieceType type, int x, int y, int z) {
        return state.pieces().stream().anyMatch(piece ->
                piece.color() == color
                        && piece.type() == type
                        && piece.position().x() == x
                        && piece.position().y() == y
                        && piece.position().z() == z);
    }

    private void withCustomBoard(UUID gameId, Consumer<Game> customizer) {
        customizer.accept(getGame(gameId));
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, Game> gamesMap() {
        try {
            Field field = GameService.class.getDeclaredField("games");
            field.setAccessible(true);
            return (Map<UUID, Game>) field.get(service);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Unable to access games map", ex);
        }
    }

    private Game getGame(UUID id) {
        Game game = gamesMap().get(id);
        if (game == null) {
            throw new IllegalStateException("Game not found in test map");
        }
        return game;
    }

    private void put(Game game, PieceType type, PieceColor color, int x, int y, int z) {
        Position position = new Position(x, y, z);
        game.getPieces().put(position, new Piece(type, color, position));
    }
}
