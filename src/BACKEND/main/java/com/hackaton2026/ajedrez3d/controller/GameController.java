package com.hackaton2026.ajedrez3d.controller;

import com.hackaton2026.ajedrez3d.dto.GameStateResponse;
import com.hackaton2026.ajedrez3d.dto.LegalMovesResponse;
import com.hackaton2026.ajedrez3d.dto.MoveRequest;
import com.hackaton2026.ajedrez3d.dto.MoveResponse;
import com.hackaton2026.ajedrez3d.security.JwtUtils;
import com.hackaton2026.ajedrez3d.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final JwtUtils jwtUtils;

    public GameController(GameService gameService, JwtUtils jwtUtils) {
        this.gameService = gameService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameStateResponse createGame(@RequestHeader("Authorization") String authHeader) {
        UUID creatorId = extractUserId(authHeader);
        return gameService.createGame(creatorId);
    }

    @PostMapping("/{id}/join")
    public GameStateResponse joinGame(@PathVariable UUID id,
                                      @RequestHeader("Authorization") String authHeader) {
        UUID joinerId = extractUserId(authHeader);
        return gameService.joinGame(id, joinerId);
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
        return gameService.getLegalMoves(id, new com.hackaton2026.ajedrez3d.model.Position(x, y, z));
    }

    @PostMapping("/{id}/moves")
    public MoveResponse move(@PathVariable UUID id,
                             @Valid @RequestBody MoveRequest request) {
        return gameService.move(id, request);
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtils.extractUserId(token);
    }
}