package com.hackaton2026.ajedrez3d.repository;

import com.hackaton2026.ajedrez3d.model.GameEntity;
import com.hackaton2026.ajedrez3d.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface GameRepository extends JpaRepository<GameEntity, UUID> {
    List<GameEntity> findByWhitePlayerOrBlackPlayerOrderByCreatedAtDesc(User white, User black);
}