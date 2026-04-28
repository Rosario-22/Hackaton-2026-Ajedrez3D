package com.hackaton2026.ajedrez3d.service;

import com.hackaton2026.ajedrez3d.dto.GameStateResponse;
import com.hackaton2026.ajedrez3d.dto.LegalMovesResponse;
import com.hackaton2026.ajedrez3d.dto.MoveRequest;
import com.hackaton2026.ajedrez3d.dto.MoveResponse;
import com.hackaton2026.ajedrez3d.model.BoardConstants;
import com.hackaton2026.ajedrez3d.model.Game;
import com.hackaton2026.ajedrez3d.model.GameEntity;
import com.hackaton2026.ajedrez3d.model.GameStatus;
import com.hackaton2026.ajedrez3d.model.MoveEntity;
import com.hackaton2026.ajedrez3d.model.MoveSummary;
import com.hackaton2026.ajedrez3d.model.Piece;
import com.hackaton2026.ajedrez3d.model.PieceColor;
import com.hackaton2026.ajedrez3d.model.PieceType;
import com.hackaton2026.ajedrez3d.model.Position;
import com.hackaton2026.ajedrez3d.model.User;
import com.hackaton2026.ajedrez3d.repository.GameRepository;
import com.hackaton2026.ajedrez3d.repository.MoveRepository;
import com.hackaton2026.ajedrez3d.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GameService {
    private final MoveRepository moveRepository;
    public static final int BOARD_SIZE = BoardConstants.BOARD_SIZE;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    private final Map<UUID, Game> games = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final MoveCalculator moveCalculator;
    private final GameStateEvaluator evaluator;
    private final GameMapper mapper;

    public GameService(SimpMessagingTemplate messagingTemplate,
                    MoveCalculator moveCalculator,
                    GameStateEvaluator evaluator,
                    GameMapper mapper,
                    GameRepository gameRepository,
                    UserRepository userRepository,
                    MoveRepository moveRepository) {
        this.messagingTemplate = messagingTemplate;
        this.moveCalculator = moveCalculator;
        this.evaluator = evaluator;
        this.mapper = mapper;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.moveRepository = moveRepository;
    }   

    public GameStateResponse createGame(UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Game game = new Game(UUID.randomUUID());
        seedPieces(game);
        games.put(game.getId(), game);

        // Guardar en base de datos
        GameEntity entity = new GameEntity();
        entity.setId(game.getId());
        entity.setWhitePlayer(creator);
        gameRepository.save(entity);

        return mapper.toStateResponse(game);
    }
        public GameStateResponse joinGame(UUID gameId, UUID joinerId) {
            Game game = getGame(gameId);

            GameEntity entity = gameRepository.findById(gameId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

            if (entity.getBlackPlayer() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already has two players");
            }

            User joiner = userRepository.findById(joinerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            entity.setBlackPlayer(joiner);
            gameRepository.save(entity);

        return mapper.toStateResponse(game);
    }

    public GameStateResponse getGameState(UUID id) {
        return mapper.toStateResponse(getGame(id));
    }

    public LegalMovesResponse getLegalMoves(UUID id, Position from) {
        Game game = getGame(id);
        Piece piece = getPiece(game, from);
        List<Position> moves = moveCalculator.legalMoves(game, piece);
        return new LegalMovesResponse(from, piece.getType().name(), moves);
    }

    public MoveResponse move(UUID id, MoveRequest request) {
        Game game = getGame(id);
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The game is already finished");
        }

        Piece movingPiece = getPiece(game, request.from());
        if (movingPiece.getColor() != game.getTurn()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It is not that piece's turn");
        }

        List<Position> legalMoves = moveCalculator.legalMoves(game, movingPiece);
        if (!legalMoves.contains(request.to())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal move");
        }

        Optional<Piece> capturedPiece = game.removePiece(request.to());
        Position originalFrom = movingPiece.getPosition();
        game.removePiece(originalFrom);
        movingPiece.setPosition(request.to());
        game.placePiece(movingPiece);

        game.setLastMove(new MoveSummary(originalFrom, request.to(), movingPiece.getType(), movingPiece.getColor()));
        game.setTurn(game.getTurn().opposite());

        if (capturedPiece.isPresent() && capturedPiece.get().getType() == PieceType.KING) {
            game.setStatus(movingPiece.getColor() == PieceColor.WHITE ? GameStatus.WHITE_WON : GameStatus.BLACK_WON);
            game.setWinner(movingPiece.getColor());
        } else {
            evaluator.updateEndState(game);
        }
        // Actualizar estado en la BD
        gameRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(game.getStatus());
            entity.setWinner(game.getWinner());
            if (game.getStatus() != GameStatus.IN_PROGRESS) {
                entity.setFinishedAt(java.time.LocalDateTime.now());
            }
            gameRepository.save(entity);
        });
        game.touch();

        // Guardar movimiento en la BD
        gameRepository.findById(id).ifPresent(entity -> {
            MoveEntity moveEntity = new MoveEntity();
            moveEntity.setGame(entity);
            moveEntity.setPieceType(movingPiece.getType());
            moveEntity.setPieceColor(movingPiece.getColor());
            moveEntity.setFromX(originalFrom.x());
            moveEntity.setFromY(originalFrom.y());
            moveEntity.setFromZ(originalFrom.z());
            moveEntity.setToX(request.to().x());
            moveEntity.setToY(request.to().y());
            moveEntity.setToZ(request.to().z());

            // Asignar el jugador según el color que movió
            if (movingPiece.getColor() == PieceColor.WHITE && entity.getWhitePlayer() != null) {
                moveEntity.setPlayer(entity.getWhitePlayer());
            } else if (entity.getBlackPlayer() != null) {
                moveEntity.setPlayer(entity.getBlackPlayer());
            }

            moveRepository.save(moveEntity);
        });

        boolean whiteInCheck = evaluator.isKingInCheck(game, PieceColor.WHITE);
        boolean blackInCheck = evaluator.isKingInCheck(game, PieceColor.BLACK);
        boolean checkmate = evaluator.isCheckmate(game);
        boolean stalemate = evaluator.isStalemate(game);
        String reason = evaluator.finishReason(game);

        MoveResponse response = new MoveResponse(
                true,
                "Move applied",
                game.getId(),
                game.getTurn(),
                game.getStatus(),
                game.getWinner(),
                whiteInCheck,
                blackInCheck,
                checkmate,
                stalemate,
                reason,
                capturedPiece.map(mapper::toDto).orElse(null),
                game.getLastMove()
        );

        messagingTemplate.convertAndSend("/topic/game/" + id, mapper.toStateResponse(game));

        return response;
    }

    private Game getGame(UUID id) {
        Game game = games.get(id);
        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        return game;
    }

    private Piece getPiece(Game game, Position position) {
        if (position == null || !position.isInsideBoard()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position is outside the 5x5x5 board");
        }
        return game.pieceAt(position)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No piece found at that position"));
    }

    private void seedPieces(Game game) {
        place(game, new Piece("1", PieceType.KING, PieceColor.WHITE, new Position(0, 0, 0)));
        place(game, new Piece("2", PieceType.ROOK, PieceColor.WHITE, new Position(1, 0, 0)));
        place(game, new Piece("3", PieceType.UNICORN, PieceColor.WHITE, new Position(0, 0, 1)));
        place(game, new Piece("4", PieceType.KNIGHT, PieceColor.WHITE, new Position(1, 0, 1)));
        place(game, new Piece("5", PieceType.BISHOP, PieceColor.WHITE, new Position(0, 1, 0)));

        place(game, new Piece("6", PieceType.KING, PieceColor.BLACK, new Position(4, 4, 4)));
        place(game, new Piece("7", PieceType.ROOK, PieceColor.BLACK, new Position(3, 4, 4)));
        place(game, new Piece("8", PieceType.UNICORN, PieceColor.BLACK, new Position(4, 4, 3)));
        place(game, new Piece("9", PieceType.KNIGHT, PieceColor.BLACK, new Position(3, 4, 3)));
        place(game, new Piece("10", PieceType.BISHOP, PieceColor.BLACK, new Position(4, 3, 4)));
    }

    private void place(Game game, Piece piece) {
        if (game.isOccupied(piece.getPosition())) {
            throw new IllegalStateException("Duplicate piece position at " + piece.getPosition());
        }
        game.placePiece(piece);
    }
    
    @jakarta.annotation.PostConstruct
    public void reloadActiveGames() {
        List<com.hackaton2026.ajedrez3d.model.GameEntity> activeGames = 
            gameRepository.findByStatus(GameStatus.IN_PROGRESS);
        
        for (com.hackaton2026.ajedrez3d.model.GameEntity entity : activeGames) {
            Game game = new Game(entity.getId());
            seedPieces(game);
            games.put(game.getId(), game);
        }
        
        System.out.println("[GameService] Partidas activas recargadas: " + activeGames.size());
    }
}