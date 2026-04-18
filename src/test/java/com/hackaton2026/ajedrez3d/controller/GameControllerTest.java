package com.hackaton2026.ajedrez3d.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hackaton2026.ajedrez3d.dto.GameStateResponse;
import com.hackaton2026.ajedrez3d.dto.LegalMovesResponse;
import com.hackaton2026.ajedrez3d.dto.MoveRequest;
import com.hackaton2026.ajedrez3d.dto.MoveResponse;
import com.hackaton2026.ajedrez3d.model.GameStatus;
import com.hackaton2026.ajedrez3d.model.MoveSummary;
import com.hackaton2026.ajedrez3d.model.PieceColor;
import com.hackaton2026.ajedrez3d.model.PieceType;
import com.hackaton2026.ajedrez3d.model.Position;
import com.hackaton2026.ajedrez3d.service.GameService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController controller;

    @Test
    void createGameDelegatesToService() {
        GameStateResponse expected = sampleState();
        when(gameService.createGame()).thenReturn(expected);

        assertSame(expected, controller.createGame());
        verify(gameService).createGame();
    }

    @Test
    void getGameDelegatesToService() {
        UUID id = UUID.randomUUID();
        GameStateResponse expected = sampleState();
        when(gameService.getGameState(id)).thenReturn(expected);

        assertSame(expected, controller.getGame(id));
        verify(gameService).getGameState(id);
    }

    @Test
    void getLegalMovesDelegatesToService() {
        UUID id = UUID.randomUUID();
        Position position = new Position(1, 2, 3);
        LegalMovesResponse expected = new LegalMovesResponse(position, "ROOK", List.of(new Position(1, 3, 3)));
        when(gameService.getLegalMoves(id, position)).thenReturn(expected);

        assertSame(expected, controller.getLegalMoves(id, 1, 2, 3));
        verify(gameService).getLegalMoves(id, position);
    }

    @Test
    void moveDelegatesToService() {
        UUID id = UUID.randomUUID();
        MoveRequest request = new MoveRequest(new Position(1, 1, 1), new Position(2, 2, 2));
        MoveResponse expected = new MoveResponse(true, "ok", id, PieceColor.BLACK, GameStatus.IN_PROGRESS, null,
                false, false, false, false, "IN_PROGRESS", null, new MoveSummary(request.from(), request.to(), PieceType.ROOK, PieceColor.WHITE));
        when(gameService.move(id, request)).thenReturn(expected);

        assertSame(expected, controller.move(id, request));
        verify(gameService).move(id, request);
    }

    private GameStateResponse sampleState() {
        return new GameStateResponse(
                UUID.randomUUID(),
                5,
                PieceColor.WHITE,
                GameStatus.IN_PROGRESS,
                null,
                false,
                false,
                true,
                true,
                false,
                false,
                "IN_PROGRESS",
                List.of(),
                null
        );
    }
}
