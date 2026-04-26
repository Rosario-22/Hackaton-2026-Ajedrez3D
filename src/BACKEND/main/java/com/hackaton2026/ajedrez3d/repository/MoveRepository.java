package com.hackaton2026.ajedrez3d.repository;

import com.hackaton2026.ajedrez3d.model.GameEntity;
import com.hackaton2026.ajedrez3d.model.MoveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MoveRepository extends JpaRepository<MoveEntity, UUID> {
    List<MoveEntity> findByGameOrderByPlayedAtAsc(GameEntity game);
}