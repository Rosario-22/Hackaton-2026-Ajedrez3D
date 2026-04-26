package com.hackaton2026.ajedrez3d.dto;

import java.util.UUID;

public record AuthResponse(
    UUID userId,
    String username,
    String plan,
    String token
) {}