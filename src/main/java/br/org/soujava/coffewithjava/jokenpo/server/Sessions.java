package br.org.soujava.coffewithjava.jokenpo.server;

import br.org.soujava.coffewithjava.jokenpo.GameOver;
import br.org.soujava.coffewithjava.jokenpo.GameState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.websocket.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApplicationScoped
public class Sessions {

    private static final Jsonb jsonb = JsonbBuilder.create();
    Map<String, Session> sessionsById = new HashMap<>();
    Map<String, PlayerData> playerDataBySession = new HashMap<>();


    public static interface EventListener extends Consumer<String> {

        default void accept(String data) {
            System.out.println("broadcast : " + data);
        }
    }

    private static final EventListener BLIND_EVENT_LISTENER = new EventListener() {
    };

    AtomicReference<EventListener> eventListener = new AtomicReference<>(BLIND_EVENT_LISTENER);

    public void register(Session session, String playerName) {
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

    public void unregister(Session session) {
        synchronized (this) {
            String sessionId = session.getId();
            sessionsById.remove(sessionId);
            playerDataBySession.remove(sessionId);
        }
    }

    public Optional<PlayerData> getPlayerData(String sessionId) {
        PlayerData value;
        synchronized (this) {
            value = playerDataBySession.get(sessionId);
        }
        return Optional.ofNullable(value);
    }

    public Optional<Session> getSession(Supplier<String> sessionIdSupplier) {
        return getSession(sessionIdSupplier.get());
    }

    public Optional<Session> getSession(String sessionId) {
        Optional<Session> sessionOfWaitingPlayer = Optional.empty();
        synchronized (this) {
            sessionOfWaitingPlayer = Optional.ofNullable(sessionsById.get(sessionId));
        }
        return sessionOfWaitingPlayer;
    }

    public void setListener(GameStageCaptureServer gameStageCaptureServer) {
        this.eventListener.set(gameStageCaptureServer);
    }

    private void sendData(String data) {
        eventListener.getAndAccumulate(BLIND_EVENT_LISTENER, (oldOne, newOne) -> {
            if (!BLIND_EVENT_LISTENER.equals(oldOne)) {
                return oldOne;
            }
            return newOne;
        }).accept(data);
    }

    public void process(GameState gameState) {
        if (gameState instanceof GameOver gameOver) {
            String json = jsonb.toJson(process(gameOver));
            sendData(json);
        }
    }

    private Object process(GameOver gameOver) {
        // TODO add playerName
        return gameOver;
    }

}
