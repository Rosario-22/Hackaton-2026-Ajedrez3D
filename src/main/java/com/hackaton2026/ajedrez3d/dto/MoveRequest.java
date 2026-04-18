package com.hackaton2026.ajedrez3d.dto;

import com.hackaton2026.ajedrez3d.model.Position;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record MoveRequest(
        @Valid @NotNull Position from,
        @Valid @NotNull Position to
) {
}
