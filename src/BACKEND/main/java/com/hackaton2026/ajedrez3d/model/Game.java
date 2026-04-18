package com.hackaton2026.ajedrez3d.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Game {

    private final UUID id;
    private final Map<Position, Piece> pieces;
    private PieceColor turn;
    private GameStatus status;
    private PieceColor winner;
    private MoveSummary lastMove;
    private final Instant createdAt;
    private Instant updatedAt;

    public Game(UUID id) {
        this.id = id;
        this.pieces = new LinkedHashMap<>();
        this.turn = PieceColor.WHITE;
        this.status = GameStatus.IN_PROGRESS;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Map<Position, Piece> getPieces() {
        return pieces;
    }

    public PieceColor getTurn() {
        return turn;
    }

    public void setTurn(PieceColor turn) {
        this.turn = turn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public PieceColor getWinner() {
        return winner;
    }

    public void setWinner(PieceColor winner) {
        this.winner = winner;
    }

    public MoveSummary getLastMove() {
        return lastMove;
    }

    public void setLastMove(MoveSummary lastMove) {
        this.lastMove = lastMove;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
