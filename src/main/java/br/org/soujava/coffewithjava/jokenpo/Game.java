package br.org.soujava.coffewithjava.jokenpo;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Game {

    private Queue<Player> waitingRoom = new LinkedList<>();
    private Map<Player, String> gamesByPlayer = new HashMap<>();
    private Map<String, Set<Player>> playersByGame = new HashMap<>();
    private Map<String, GameState> games = new HashMap<>();


    public GameState newGame(Player player) {
        synchronized (this) {
            var playerWaiting = waitingRoom.poll();
            if (playerWaiting == null || Objects.equals(player, playerWaiting)) {
                var gameId = gamesByPlayer.computeIfAbsent(player, k -> UUID.randomUUID().toString());
                playersByGame.computeIfAbsent(gameId, k -> new LinkedHashSet<>()).add(player);
                waitingRoom.offer(player);
                return new WaitingPlayers(gameId, player);
            }

            var gameId = gamesByPlayer.computeIfAbsent(playerWaiting, k -> UUID.randomUUID().toString());
            playersByGame.computeIfAbsent(gameId, k -> new LinkedHashSet<>()).add(player);
            return games.merge(gameId, new GameReady(gameId, playerWaiting, player), (oldState, newState) -> newState);
        }
    }

    public GameState playGame(String gameId, Player player, Movement movement) {

        AtomicReference<GameState> gameState = new AtomicReference<>(null);
        synchronized (this) {
            games.computeIfPresent(gameId, (key, oldState) -> {
                GameState newState = null;
                if (oldState instanceof GameReady gameReady) {
                    newState = play(gameReady, player, movement);
                }
                if (oldState instanceof GameRunning gameRunning) {
                    newState = play(gameRunning, player, movement);
                }
                gameState.set(newState);
                if (newState instanceof GameOver gameOver) {
                    playersByGame.remove(gameId)
                            .forEach(gamesByPlayer::remove);
                    return null;
                }
                return newState;
            });
        }
        return Optional.ofNullable(gameState.get()).orElseGet(() -> new InvalidGame(gameId));
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
}