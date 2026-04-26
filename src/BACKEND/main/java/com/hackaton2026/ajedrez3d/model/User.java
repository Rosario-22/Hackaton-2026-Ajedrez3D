package com.hackaton2026.ajedrez3d.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserPlan plan = UserPlan.FREE;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters y Setters
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserPlan getPlan() { return plan; }
    public void setPlan(UserPlan plan) { this.plan = plan; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
