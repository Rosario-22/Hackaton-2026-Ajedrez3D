package com.hackaton2026.ajedrez3d.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "moves")
public class MoveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PieceType pieceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PieceColor pieceColor;

    @Column(nullable = false)
    private int fromX, fromY, fromZ;

    @Column(nullable = false)
    private int toX, toY, toZ;

    @Column(nullable = false)
    private LocalDateTime playedAt = LocalDateTime.now();

    // Getters y Setters
    public UUID getId() { return id; }
    public GameEntity getGame() { return game; }
    public void setGame(GameEntity game) { this.game = game; }
    public User getPlayer() { return player; }
    public void setPlayer(User player) { this.player = player; }
    public PieceType getPieceType() { return pieceType; }
    public void setPieceType(PieceType pieceType) { this.pieceType = pieceType; }
    public PieceColor getPieceColor() { return pieceColor; }
    public void setPieceColor(PieceColor pieceColor) { this.pieceColor = pieceColor; }
    public int getFromX() { return fromX; }
    public void setFromX(int fromX) { this.fromX = fromX; }
    public int getFromY() { return fromY; }
    public void setFromY(int fromY) { this.fromY = fromY; }
    public int getFromZ() { return fromZ; }
    public void setFromZ(int fromZ) { this.fromZ = fromZ; }
    public int getToX() { return toX; }
    public void setToX(int toX) { this.toX = toX; }
    public int getToY() { return toY; }
    public void setToY(int toY) { this.toY = toY; }
    public int getToZ() { return toZ; }
    public void setToZ(int toZ) { this.toZ = toZ; }
    public LocalDateTime getPlayedAt() { return playedAt; }
}
