package com.hackaton2026.ajedrez3d.service;

import com.hackaton2026.ajedrez3d.dto.AuthResponse;
import com.hackaton2026.ajedrez3d.dto.LoginRequest;
import com.hackaton2026.ajedrez3d.dto.RegisterRequest;
import com.hackaton2026.ajedrez3d.model.User;
import com.hackaton2026.ajedrez3d.repository.UserRepository;
import com.hackaton2026.ajedrez3d.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getPlan().name());
        return new AuthResponse(user.getId(), user.getUsername(), user.getPlan().name(), token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getPlan().name());
        return new AuthResponse(user.getId(), user.getUsername(), user.getPlan().name(), token);
    }
}