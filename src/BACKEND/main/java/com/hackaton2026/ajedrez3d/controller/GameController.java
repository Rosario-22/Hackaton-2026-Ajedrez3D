package com.hackaton2026.ajedrez3d.controller;

import com.hackaton2026.ajedrez3d.dto.GameStateResponse;
import com.hackaton2026.ajedrez3d.dto.LegalMovesResponse;
import com.hackaton2026.ajedrez3d.dto.MoveRequest;
import com.hackaton2026.ajedrez3d.dto.MoveResponse;
import com.hackaton2026.ajedrez3d.model.Position;
import com.hackaton2026.ajedrez3d.service.GameService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public GameStateResponse createGame() {
        return gameService.createGame();
    }

    @GetMapping("/{id}")
    public GameStateResponse getGame(@PathVariable UUID id) {
        return gameService.getGameState(id);
    }

    @GetMapping("/{id}/legal-moves")
    public LegalMovesResponse getLegalMoves(@PathVariable UUID id,
                                            @RequestParam int x,
                                            @RequestParam int y,
                                            @RequestParam int z) {
        return gameService.getLegalMoves(id, new Position(x, y, z));
    }

    @PostMapping("/{id}/moves")
    public MoveResponse move(@PathVariable UUID id, @Valid @RequestBody MoveRequest request) {
        return gameService.move(id, request);
    }
}
