package com.hackaton2026.ajedrez3d.model;

public class Piece {

    private final String id;
    private final PieceType type;
    private final PieceColor color;
    private Position position;

    public Piece(String id, PieceType type, PieceColor color, Position position) {
        this.id = id;
        this.type = type;
        this.color = color;
        this.position = position;
    }

    public String getId() {
        return id;
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
