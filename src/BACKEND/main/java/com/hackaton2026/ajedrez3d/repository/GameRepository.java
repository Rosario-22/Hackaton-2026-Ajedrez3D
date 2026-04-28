package com.hackaton2026.ajedrez3d.repository;

import com.hackaton2026.ajedrez3d.model.GameEntity;
import com.hackaton2026.ajedrez3d.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;
 import com.hackaton2026.ajedrez3d.model.GameStatus;

public interface GameRepository extends JpaRepository<GameEntity, UUID> {
    List<GameEntity> findByWhitePlayerOrBlackPlayerOrderByCreatedAtDesc(User white, User black);
    List<GameEntity> findByStatus(GameStatus status);
   
    @Query("SELECT g FROM GameEntity g WHERE g.blackPlayer IS NULL AND g.status = 'IN_PROGRESS'")
    List<GameEntity> findAvailableGames();
}