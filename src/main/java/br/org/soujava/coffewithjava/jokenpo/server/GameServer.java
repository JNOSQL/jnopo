package br.org.soujava.coffewithjava.jokenpo.server;

import br.org.soujava.coffewithjava.jokenpo.Game;
import br.org.soujava.coffewithjava.jokenpo.GameAbandoned;
import br.org.soujava.coffewithjava.jokenpo.GameInvalid;
import br.org.soujava.coffewithjava.jokenpo.GameOver;
import br.org.soujava.coffewithjava.jokenpo.GameOverLoserInfo;
import br.org.soujava.coffewithjava.jokenpo.GameOverPlayerInfo;
import br.org.soujava.coffewithjava.jokenpo.GameOverWinnerInfo;
import br.org.soujava.coffewithjava.jokenpo.GameReady;
import br.org.soujava.coffewithjava.jokenpo.GameRunning;
import br.org.soujava.coffewithjava.jokenpo.GameState;
import br.org.soujava.coffewithjava.jokenpo.Movement;
import br.org.soujava.coffewithjava.jokenpo.Player;
import br.org.soujava.coffewithjava.jokenpo.WaitingPlayers;
import br.org.soujava.coffewithjava.jokenpo.server.Message.Field;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static br.org.soujava.coffewithjava.jokenpo.server.Message.Field.gameId;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Field.messageSetter;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Field.opponentMovement;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Field.opponentName;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Type.CONNECTED;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Type.GAME_INVALID;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Type.GAME_OVER_DRAW;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Type.GAME_OVER_YOU_LOSE;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Type.GAME_OVER_YOU_WIN;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Type.GAME_READY;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Type.GAME_RUNNING;
import static br.org.soujava.coffewithjava.jokenpo.server.Message.Type.WAITING_PLAYERS;
import static java.util.Objects.isNull;

@ServerEndpoint("/jokenpo/{playerName}")
@ApplicationScoped
public class GameServer {

    private static final Logger LOG = Logger.getLogger(GameServer.class);

    Map<String, Session> sessionsById = new HashMap<>();
    Map<String, PlayerData> playerDataBySession = new HashMap<>();
    Game game = new Game();

    public record PlayerData(String sessionId, Map<String, String> data) {
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("playerName") String playerName) {
        register(session, playerName);
        session.getAsyncRemote().sendText(CONNECTED.build().toJson());
    }

    private Optional<Session> getSession(Supplier<String> sessionIdSupplier) {
        return getSession(sessionIdSupplier.get());
    }

    private Optional<Session> getSession(String sessionId) {
        Optional<Session> sessionOfWaitingPlayer = Optional.empty();
        synchronized (this) {
            sessionOfWaitingPlayer = Optional.ofNullable(sessionsById.get(sessionId));
        }
        return sessionOfWaitingPlayer;
    }

    private Optional<PlayerData> getPlayerData(String sessionId) {
        PlayerData value;
        synchronized (this) {
            value = playerDataBySession.get(sessionId);
        }
        return Optional.ofNullable(value);
    }

    private void register(Session session, String playerName) {
        synchronized (this) {
            String sessionId = session.getId();
            sessionsById.put(sessionId, session);
            playerDataBySession.merge(
                    sessionId,
                    new PlayerData(sessionId,
                            Map.of("id", sessionId, "name", playerName)), this::mergePlayerData);
        }
    }

    private PlayerData mergePlayerData(PlayerData oldData, PlayerData newData) {
        var data = new HashMap<>(oldData.data());
        newData.data()
                .forEach((key, value) ->
                        data.merge(key, value, (a, b) -> b));

        return new PlayerData(newData.sessionId(), data);
    }

    @OnClose
    public void onClose(Session session, @PathParam("playerName") String playerName) {
        unregister(session);
    }

    private void unregister(Session session) {
        leavingGame(session);
        synchronized (this) {
            String sessionId = session.getId();
            sessionsById.remove(sessionId);
            playerDataBySession.remove(sessionId);
        }
    }

    private void leavingGame(Session session) {
        GameState gameState = game.leavingGame(new Player(session.getId()));
        if (gameState instanceof GameInvalid gameInvalid) {
            send(session, GAME_INVALID.build(m -> m.set(gameId, gameInvalid.gameId())));
        }
        if (gameState instanceof GameAbandoned gameAbandoned) {
            Player playerSession = Player.of(session.getId());
            gameAbandoned.players().stream()
                    .filter(p -> !playerSession.equals(p))
                    .map(Player::id)
                    .map(this::getSession)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(opponentSession -> {
                        send(opponentSession, gameAbandoned);
                    });
        }

    }


    private void send(Session session, Message message) {
        if (session.isOpen()) {
            session.getAsyncRemote().sendText(message.toJson());
        } else {
            unregister(session);
        }
    }

    @OnError
    public void onError(Session session, @PathParam("playerName") String playerName, Throwable throwable) {
        unregister(session);
        LOG.error("onError", throwable);
    }

    @OnMessage
    public void onMessage(String rawMessage, Session session) {
        var message = Message.fromJson(rawMessage);
        if (isNull(message.type())) {
            receivedInvalidMessage(rawMessage, session);
            return;
        }
        switch (message.type()) {
            case NEW_GAME -> playerWantNewGame(message, session);
            case PLAY_GAME -> playerWantsToPlay(message, session);
            default -> receivedUnsupportedMessage(message, session);
        }
    }

    private void receivedUnsupportedMessage(Message message, Session session) {
        LOG.error("received an unsupported message: " + message.toJson());
    }

    private void receivedInvalidMessage(String rawMessage, Session session) {
        LOG.error("received an invalid message: " + rawMessage);
    }

    private void playerWantNewGame(Message message, Session playerSession) {
        GameState gameState = game.newGame(new Player(playerSession.getId()));
        if (gameState instanceof WaitingPlayers waitingPlayers) {
            send(playerSession, waitingPlayers);
        }
        if (gameState instanceof GameReady gameReady) {
            getSession(gameReady.playerA()::id)
                    .ifPresent(session -> {
                        send(session, gameReady);
                    });
            getSession(gameReady.playerB()::id)
                    .ifPresent(session -> {
                        send(session, gameReady);
                    });
        }

        if (gameState instanceof GameRunning gameRunning) {
            getSession(gameRunning.playerA()::id)
                    .ifPresent(session -> {
                        send(session, gameRunning);
                    });
            getSession(gameRunning.playerB()::id)
                    .ifPresent(session -> {
                        send(session, gameRunning);
                    });
        }
    }


    private void playerWantsToPlay(Message message, Session playerSession) {
        Player sessionPlayer = Player.of(playerSession.getId());
        Field.gameId.getOptional(message)
                .ifPresent(gameId -> {
                    GameState gameState = Field.movement.getOptional(message)
                            .map(Movement::valueOf)
                            .map(movement -> game.playGame(
                                    gameId,
                                    sessionPlayer,
                                    movement))
                            .orElseGet(() -> new GameInvalid(gameId));

                    if (gameState instanceof GameInvalid gameInvalid) {
                        send(playerSession, gameInvalid);
                    }
                    if (gameState instanceof GameRunning gameRunning) {
                        getSession(gameRunning.playerA()::id)
                                .ifPresent(session -> {
                                    send(session, gameRunning);
                                });
                        getSession(gameRunning.playerB()::id)
                                .ifPresent(session -> {
                                    send(session, gameRunning);
                                });
                    }
                    if (gameState instanceof GameOver gameOver) {

                        boolean isPlayerA = sessionPlayer.equals(gameOver.playerA());

                        if (gameOver.isTied()) {
                            getSession(sessionPlayer.id())
                                    .ifPresent(session -> {
                                        send(session, GAME_OVER_DRAW, isPlayerA ? gameOver.playerBInfo() : gameOver.playerAInfo());
                                    });
                            getSession(isPlayerA ? gameOver.playerB().id() : gameOver.playerA().id())
                                    .ifPresent(session -> {
                                        send(session, GAME_OVER_DRAW, isPlayerA ? gameOver.playerAInfo() : gameOver.playerBInfo());
                                    });
                        }

                        gameOver.winner().ifPresent(winner -> {
                            boolean isWinner = sessionPlayer.equals(winner);
                            getSession(sessionPlayer.id())
                                    .ifPresent(session -> {
                                        if (isWinner) {
                                            send(session, GAME_OVER_YOU_WIN, gameOver.loserInfo().orElse(null));
                                        } else {
                                            send(session, GAME_OVER_YOU_LOSE, gameOver.winnerInfo().orElse(null));
                                        }
                                    });

                            getSession(isPlayerA ? gameOver.playerB().id() : gameOver.playerA().id())
                                    .ifPresent(session -> {
                                        if (isWinner) {
                                            send(session, GAME_OVER_YOU_LOSE, gameOver.winnerInfo().orElse(null));
                                        } else {
                                            send(session, GAME_OVER_YOU_WIN, gameOver.loserInfo().orElse(null));
                                        }
                                    });
                        });
                    }
                });
    }

    private void send(Session session, Message.Type type, GameOverLoserInfo gameOverPlayerInfo) {
        send(session, type, new GameOverPlayerInfo(gameOverPlayerInfo));
    }

    private void send(Session session, Message.Type type, GameOverWinnerInfo gameOverWinnerInfo) {
        send(session, type, new GameOverPlayerInfo(gameOverWinnerInfo));
    }

    private void send(Session session, Message.Type type, GameOverPlayerInfo gameOverPlayerInfo) {

        Message message = type.build(s -> s.set(gameId, gameOverPlayerInfo.gameId()));

        getPlayerData(gameOverPlayerInfo.player().id())
                .map(PlayerData::data)
                .ifPresent(playerData -> {
                    messageSetter(message)
                            .set(opponentName, playerData.getOrDefault("name", "unknown"))
                            .set(opponentMovement, gameOverPlayerInfo.movement().name());
                });
        send(session, message);
    }


    private void send(Session session, GameInvalid gameInvalid) {
        send(session, GAME_INVALID.build(s -> s.set(gameId, gameInvalid.gameId())));
    }

    private void send(Session session, GameRunning gameRunning) {
        var playerSession = Player.of(session.getId());
        var playerData = getPlayerData(playerSession.id());

        gameRunning.players()
                .filter(player -> !playerSession.equals(player))
                .forEach(opponent -> {
                    Message messageToOpponent = GAME_RUNNING.build(s -> s.set(gameId, gameRunning.gameId()));
                    playerData.map(PlayerData::data)
                            .ifPresent(data -> messageSetter(messageToOpponent)
                                    .set(opponentName, data.getOrDefault("name", "unknown")));
                    getSession(opponent.id())
                            .ifPresent(opponentSession -> send(opponentSession, messageToOpponent));

                    Message messageToOriginator = GAME_RUNNING.build(s -> s.set(gameId, gameRunning.gameId()));

                    getPlayerData(opponent.id())
                            .map(PlayerData::data)
                            .ifPresent(data -> messageSetter(messageToOriginator)
                                    .set(opponentName, data.getOrDefault("name", "unknown")));

                    send(session, messageToOriginator);

                });
    }

    private void send(Session session, GameReady gameReady) {
        var playerSession = Player.of(session.getId());
        var playerData = getPlayerData(playerSession.id());

        gameReady.players()
                .filter(player -> !playerSession.equals(player))
                .forEach(opponent -> {
                    Message messageToOpponent = GAME_READY.build(s -> s.set(gameId, gameReady.gameId()));
                    playerData.map(PlayerData::data)
                            .ifPresent(data -> messageSetter(messageToOpponent)
                                    .set(opponentName, data.getOrDefault("name", "unknown")));
                    getSession(opponent.id())
                            .ifPresent(opponentSession -> send(opponentSession, messageToOpponent));

                    Message messageToOriginator = GAME_READY.build(s -> s.set(gameId, gameReady.gameId()));

                    getPlayerData(opponent.id())
                            .map(PlayerData::data)
                            .ifPresent(data -> messageSetter(messageToOriginator)
                                    .set(opponentName, data.getOrDefault("name", "unknown")));

                    send(session, messageToOriginator);

                });

    }

    private void send(Session session, WaitingPlayers waitingPlayers) {
        var playerSession = Player.of(session.getId());

        Message message = WAITING_PLAYERS.build(m -> m.set(gameId, waitingPlayers.gameId()));

        send(session, message);
    }

    private void send(Session session, GameAbandoned gameAbandoned) {


        Message message = Message.Type.GAME_OVER_ABANDONED.build(m -> m.set(gameId, gameAbandoned.gameId()));


        send(session, message);
    }
}
