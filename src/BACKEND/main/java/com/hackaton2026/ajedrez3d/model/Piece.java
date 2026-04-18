package com.hackaton2026.ajedrez3d.model;

public class Piece {

    private final PieceType type;
    private final PieceColor color;
    private Position position;

    public Piece(PieceType type, PieceColor color, Position position) {
        this.type = type;
        this.color = color;
        this.position = position;
    }

    public PieceType getType() {
        return type;
    }

    public PieceColor getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
