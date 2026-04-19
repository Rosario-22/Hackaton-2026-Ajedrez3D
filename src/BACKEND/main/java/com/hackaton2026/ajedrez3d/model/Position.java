package com.hackaton2026.ajedrez3d.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record Position(
        @Min(0) @Max(4) int x,
        @Min(0) @Max(4) int y,
        @Min(0) @Max(4) int z
) {
    public boolean isInsideBoard() {
        return x >= 0 && x < BoardConstants.BOARD_SIZE
            && y >= 0 && y < BoardConstants.BOARD_SIZE
            && z >= 0 && z < BoardConstants.BOARD_SIZE;
    }
}