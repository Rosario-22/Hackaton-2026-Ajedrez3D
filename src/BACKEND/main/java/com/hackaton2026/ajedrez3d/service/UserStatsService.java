package com.hackaton2026.ajedrez3d.service;

import com.hackaton2026.ajedrez3d.dto.GameHistoryResponse;
import com.hackaton2026.ajedrez3d.dto.UserStatsResponse;
import com.hackaton2026.ajedrez3d.model.GameEntity;
import com.hackaton2026.ajedrez3d.model.GameStatus;
import com.hackaton2026.ajedrez3d.model.PieceColor;
import com.hackaton2026.ajedrez3d.model.User;
import com.hackaton2026.ajedrez3d.repository.GameRepository;
import com.hackaton2026.ajedrez3d.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserStatsService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public UserStatsService(UserRepository userRepository, GameRepository gameRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    public UserStatsResponse getStats(UUID userId) {
        User user = getUser(userId);
        List<GameEntity> games = gameRepository
                .findByWhitePlayerOrBlackPlayerOrderByCreatedAtDesc(user, user);

        long played = games.size();
        long won = games.stream().filter(g -> isWinner(g, user)).count();
        long lost = games.stream().filter(g -> isLoser(g, user)).count();
        long draws = games.stream().filter(g -> g.getStatus() == GameStatus.DRAW).count();

        return new UserStatsResponse(userId, user.getUsername(), played, won, lost, draws);
    }

    public List<GameHistoryResponse> getGameHistory(UUID userId) {
        User user = getUser(userId);
        List<GameEntity> games = gameRepository
                .findByWhitePlayerOrBlackPlayerOrderByCreatedAtDesc(user, user);

        return games.stream().map(g -> new GameHistoryResponse(
                g.getId(),
                g.getWhitePlayer() != null ? g.getWhitePlayer().getUsername() : "?",
                g.getBlackPlayer() != null ? g.getBlackPlayer().getUsername() : "?",
                g.getStatus().name(),
                g.getWinner() != null ? g.getWinner().name() : null,
                g.getCreatedAt(),
                g.getFinishedAt()
        )).toList();
    }

    private boolean isWinner(GameEntity game, User user) {
        if (game.getWinner() == null) return false;
        if (game.getWinner() == PieceColor.WHITE) return user.equals(game.getWhitePlayer());
        return user.equals(game.getBlackPlayer());
    }

    private boolean isLoser(GameEntity game, User user) {
        if (game.getWinner() == null) return false;
        return !isWinner(game, user);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
