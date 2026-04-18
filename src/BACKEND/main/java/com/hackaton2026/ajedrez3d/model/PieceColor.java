package com.hackaton2026.ajedrez3d.model;

public enum PieceColor {
    WHITE,
    BLACK;

    public PieceColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
