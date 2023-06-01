package br.org.soujava.coffewithjava.jnopo.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;


public class Game {

    private final Queue<Player> waitingRoom = new LinkedList<>();
    private final Map<Player, String> gamesByPlayer = new HashMap<>();
    private final Map<String, Set<Player>> playersByGame = new HashMap<>();
    private final Map<String, GameState> games = new HashMap<>();

    public GameState newGame(Player player) {
        requireNonNull(player, "player is required");
        synchronized (this) {
            var playerWaiting = waitingRoom.poll();
            if (isNull(playerWaiting) || player.equals(playerWaiting)) {
                var gameId = gamesByPlayer.computeIfAbsent(player, k -> UUID.randomUUID().toString());
                playersByGame.computeIfAbsent(gameId, k -> new LinkedHashSet<>()).add(player);
                waitingRoom.offer(player);
                return games.merge(gameId, new WaitingPlayers(gameId, player), (oldState, newState) -> newState);
            }
            var gameId = gamesByPlayer.computeIfAbsent(playerWaiting, k -> UUID.randomUUID().toString());
            gamesByPlayer.computeIfAbsent(player, k -> gameId);
            playersByGame.computeIfAbsent(gameId, k -> new LinkedHashSet<>()).add(player);
            return games.merge(gameId, new GameReady(gameId, playerWaiting, player), (oldState, newState) -> newState);
        }
    }


    public GameState playGame(String gameId, Player player, Movement movement) {
        requireNonNull(gameId, "gameId is required");
        requireNonNull(player, "player is required");
        requireNonNull(movement, "movement is required");
        synchronized (this) {
            var newState = games.computeIfPresent(gameId, (key, oldState) -> {
                if (oldState instanceof GameReady gameReady) {
                    return play(gameReady, player, movement);
                }
                if (oldState instanceof GameRunning gameRunning) {
                    return play(gameRunning, player, movement);
                }
                return oldState;
            });
            if (newState instanceof GameOver) {
                playersByGame.remove(gameId)
                        .forEach(gamesByPlayer::remove);
                games.remove(gameId);
            }
            return Optional.ofNullable(newState).orElseGet(() -> new GameInvalid(gameId));
        }
    }

    private GameState play(GameReady gameReady, Player player, Movement movement) {
        var isPlayerA = Objects.equals(gameReady.playerA(), player);
        var isPlayerB = Objects.equals(gameReady.playerB(), player);
        if (!isPlayerA && !isPlayerB) {
            return null;
        }
        return new GameRunning(gameReady.gameId(),
                gameReady.playerA(),
                gameReady.playerB(),
                isPlayerA ? movement : null,
                isPlayerB ? movement : null);

    }

    private GameState play(GameRunning gameRunning, Player player, Movement movement) {
        var isPlayerA = Objects.equals(gameRunning.playerA(), player);
        var isPlayerB = Objects.equals(gameRunning.playerB(), player);
        if (!isPlayerA && !isPlayerB) {
            return null;
        }
        var opponentMovement = isPlayerA ? gameRunning.playerBMovement() : gameRunning.playerAMovement();
        if (opponentMovement == null) {
            return new GameRunning(gameRunning.gameId(),
                    gameRunning.playerA(),
                    gameRunning.playerB(),
                    isPlayerA ? movement : null,
                    isPlayerB ? movement : null);
        }
        return new GameOver(gameRunning.gameId(),
                gameRunning.playerA(),
                gameRunning.playerB(),
                isPlayerA ? movement : gameRunning.playerAMovement(),
                isPlayerB ? movement : gameRunning.playerBMovement());
    }


    public Set<Player> playersByGame(String gameId) {
        synchronized (this) {
            return Collections.unmodifiableSet(playersByGame.getOrDefault(gameId, Set.of()));
        }
    }

    public GameState getGameState(String gameId) {
        if (isNull(gameId)) {
            return new GameInvalid(null);
        }
        synchronized (this) {
            return Optional.ofNullable(games.get(gameId)).orElse(new GameInvalid(gameId));
        }
    }

    public GameState leavingGame(Player player) {
        if (isNull(player)) {
            return new GameInvalid(null);
        }
        synchronized (this) {
            var gameId = gamesByPlayer.remove(player);
            if (isNull(gameId)) {
                return new GameInvalid(null);
            }
            Set<Player> players = playersByGame.getOrDefault(gameId, new LinkedHashSet<>());
            players.remove(player);
            Player opponent = null;
            if (!players.isEmpty()) {
                opponent = players.iterator().next();
                gamesByPlayer.remove(opponent);
                games.remove(gameId);
            }
            return new GameAbandoned(gameId, Stream.of(player, opponent).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet()));
        }
    }

    public Stream<Player> getWaitingRoom() {
        synchronized (this) {
            return this.waitingRoom.stream();
        }
    }
}