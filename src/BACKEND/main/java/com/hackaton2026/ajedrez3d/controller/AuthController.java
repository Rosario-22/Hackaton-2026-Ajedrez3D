package com.hackaton2026.ajedrez3d.controller;

import com.hackaton2026.ajedrez3d.dto.AuthResponse;
import com.hackaton2026.ajedrez3d.dto.LoginRequest;
import com.hackaton2026.ajedrez3d.dto.RegisterRequest;
import com.hackaton2026.ajedrez3d.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}