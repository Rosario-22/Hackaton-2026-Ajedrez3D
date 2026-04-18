package com.hackaton2026.ajedrez3d.dto;

import com.hackaton2026.ajedrez3d.model.PieceType;
import java.util.List;

public record PieceRuleDto(
        PieceType type,
        String description,
        List<String> movement
) {
}
