package br.org.soujava.coffewithjava.jokenpo;

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

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;


public class Game {

    private final Queue<Player> waitingRoom = new LinkedList<>();
    private final Map<Player, String> gamesByPlayer = new HashMap<>();
    private final Map<String, Set<Player>> playersByGame = new HashMap<>();
    private final Map<String, GameState> games = new HashMap<>();


    public GameState newGame(Player player) {
        requireNonNull(player,"player is required");
        synchronized (this) {
            var playerWaiting = waitingRoom.poll();
            if (isNull(playerWaiting) || player.equals(playerWaiting)) {
                var gameId = getGameId(player);
                getPlayersOfGame(gameId).add(player);
                waitingRoom.offer(player);
                return new WaitingPlayers(gameId, player);
            }
            var gameId = getGameId(playerWaiting);
            getPlayersOfGame(gameId).add(player);
            return games.merge(gameId, new GameReady(gameId, playerWaiting, player), (oldState, newState) -> newState);
        }
    }

    private Set<Player> getPlayersOfGame(String gameId) {
        return playersByGame.computeIfAbsent(gameId, k -> new LinkedHashSet<>());
    }

    private String getGameId(Player player) {
        return gamesByPlayer.computeIfAbsent(player, k -> UUID.randomUUID().toString());
    }

    public GameState playGame(String gameId, Player player, Movement movement) {
        requireNonNull(gameId,"gameId is required");
        requireNonNull(player,"player is required");
        requireNonNull(movement,"movement is required");
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
            return Optional.ofNullable(newState).orElseGet(() -> new InvalidGame(gameId));
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
}