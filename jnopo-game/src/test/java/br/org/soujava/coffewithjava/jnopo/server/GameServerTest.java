package br.org.soujava.coffewithjava.jnopo.server;

import br.org.soujava.coffewithjava.jnopo.core.Movement;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@QuarkusTest
public class GameServerTest {

    @TestHTTPResource("/jnopo/player%201")
    URI player1URI;
    @TestHTTPResource("/jnopo/player%202")
    URI player2URI;


    private static final Map<String, LinkedBlockingDeque<String>> MESSAGES = new ConcurrentHashMap<>();

    @BeforeAll
    @AfterAll
    public static void resetMESSAGES() {
        MESSAGES.clear();
    }


    static LinkedBlockingDeque<String> messagesFrom(Session session) {
        return messagesFrom(session.getId());
    }

    static LinkedBlockingDeque<String> messagesFrom(String sessionId) {
        return MESSAGES.computeIfAbsent(sessionId, k -> new LinkedBlockingDeque<>());
    }

    static Message receiveMessage(Session session, int timeout, TimeUnit unit) throws InterruptedException {
        String data = receiveRawMessage(session, timeout, unit);
        return Message.fromJson(data);
    }

    static Message waitToReceiveMessage(Session session, Predicate<Message> predicate) throws InterruptedException {
        return waitToReceiveMessage(session, predicate, 60, TimeUnit.SECONDS);
    }

    static Message waitToReceiveMessage(Session session, Predicate<Message> predicate, int timeout, TimeUnit unit) throws InterruptedException {
        Message message;
        do {
            String data = receiveRawMessage(session, timeout, unit);
            Objects.requireNonNull(data, "no message received in " + timeout + " " + unit);
            message = Message.fromJson(data);
        } while (!predicate.test(message));
        return message;
    }

    private static Message receiveMessage(Session session) throws InterruptedException {
        return receiveMessage(session, 10, TimeUnit.SECONDS);
    }

    private static String receiveRawMessage(Session session, int timeout, TimeUnit unit) throws InterruptedException {
        return messagesFrom(session).poll(timeout, unit);
    }

    private static String waitToReceiveRawMessage(Session session, Predicate<String> predicate) throws InterruptedException {
        return waitToReceiveRawMessage(session, predicate, 10, TimeUnit.SECONDS);
    }

    private static String waitToReceiveRawMessage(Session session, Predicate<String> predicate, int timeout, TimeUnit unit) throws InterruptedException {
        String message;
        do {
            message = receiveRawMessage(session, timeout, unit);
            Objects.requireNonNull(message, "no message received in " + timeout + " " + unit);
        } while (!predicate.test(message));
        return message;
    }

    private static String receiveRawMessage(Session session) throws InterruptedException {
        return receiveRawMessage(session, 10, TimeUnit.SECONDS);
    }


    @Test
    void shouldGameOverWithWinner() throws Exception {
        try (Session session1 = getSession(player1URI);
             Session session2 = getSession(player2URI)) {

            assertSoftly(softly -> {
                try {

                    softly.assertThat(receiveRawMessage(session1))
                            .as("session1 should be connected")
                            .isEqualTo("OKAY");

                    softly.assertThat(receiveRawMessage(session2))
                            .as("session2 should be connected")
                            .isEqualTo("OKAY");

                    softly.assertThat(waitToReceiveMessage(session1, message -> Message.Type.CONNECTED.equals(message.type())))
                            .as("session1 should have connection established")
                            .isNotNull();

                    softly.assertThat(waitToReceiveMessage(session2, message -> Message.Type.CONNECTED.equals(message.type())))
                            .as("session2 should have connection established")
                            .isNotNull();

                    send(session1, Message.Type.NEW_GAME.build());

                    softly.assertThat(waitToReceiveMessage(session1, message -> Message.Type.WAITING_PLAYERS.equals(message.type())))
                            .as("session1 game should be waiting for players")
                            .isNotNull();

                    send(session2, Message.Type.NEW_GAME.build());

                    Message gameReadyFromSession2;
                    softly.assertThat(gameReadyFromSession2 = waitToReceiveMessage(session2, message -> Message.Type.GAME_READY.equals(message.type())))
                            .as(Message.Type.GAME_READY + " received from session2")
                            .isNotNull();

                    softly.assertThat(Message.Field.opponentName.get(gameReadyFromSession2))
                            .as(" opponent of player2 should be player 1")
                            .isEqualTo("player 1");

                    var gameIdSession2 = Message.Field.gameId.get(gameReadyFromSession2);

                    Message gameReadyFromSession1;

                    softly.assertThat(gameReadyFromSession1 = waitToReceiveMessage(session1, message -> Message.Type.GAME_READY.equals(message.type())))
                            .as(Message.Type.GAME_READY + " received from session1")
                            .isNotNull();

                    softly.assertThat(Message.Field.opponentName.get(gameReadyFromSession1))
                            .as("opponent of player 1 should be player 2")
                            .isEqualTo("player 2");

                    var gameIdSession1 = Message.Field.gameId.get(gameReadyFromSession1);

                    softly.assertThat(gameIdSession1)
                            .as("gameId should be the same")
                            .isEqualTo(gameIdSession2);


                    send(session1, Message.Type.PLAY_GAME.build(messageSetter -> {
                        messageSetter.set(Message.Field.gameId, gameIdSession1)
                                .set(Message.Field.movement, Movement.ROCK.name());
                    }));

                    softly.assertThat(waitToReceiveMessage(session1, m -> Message.Type.GAME_RUNNING.equals(m.type())))
                            .as(Message.Type.GAME_RUNNING + " received from session1")
                            .isNotNull();

                    softly.assertThat(waitToReceiveMessage(session2, m -> Message.Type.GAME_RUNNING.equals(m.type())))
                            .as(Message.Type.GAME_RUNNING + " received from session2")
                            .isNotNull();

                    send(session2, Message.Type.PLAY_GAME.build(messageSetter -> {
                        messageSetter.set(Message.Field.gameId, gameIdSession1)
                                .set(Message.Field.movement, Movement.SCISSORS.name());
                    }));

                    Message gameOverSession1 = waitToReceiveMessage(session1, m -> Message.Type.GAME_OVER_YOU_WIN.equals(m.type()));
                    softly.assertThat(gameOverSession1)
                            .as(Message.Type.GAME_OVER_YOU_WIN + " received from session2")
                            .isNotNull();

                    softly.assertThat(Message.Field.opponentName.get(gameOverSession1))
                            .as("loser should be player2")
                            .isEqualTo("player 2");

                    softly.assertThat(Message.Field.opponentMovement.get(gameOverSession1))
                            .as("loser movement should be " + Movement.SCISSORS.name())
                            .isEqualTo(Movement.SCISSORS.name());


                    Message gameOverSession2 = waitToReceiveMessage(session2, m -> Message.Type.GAME_OVER_YOU_LOSE.equals(m.type()));

                    softly.assertThat(gameOverSession2)
                            .as(Message.Type.GAME_OVER_YOU_LOSE + " received from session2")
                            .isNotNull();

                    softly.assertThat(Message.Field.opponentName.get(gameOverSession2))
                            .as("winner should be player 1")
                            .isNotNull();

                    softly.assertThat(Message.Field.opponentMovement.get(gameOverSession2))
                            .as("loser movement should be " + Movement.ROCK.name())
                            .isEqualTo(Movement.ROCK.name());

                    System.out.println("fim");
                } catch (InterruptedException e) {
                    softly.fail(e.getMessage(), e);
                }
            });
        }
    }


    @Test
    void shouldGameOverDraw() throws Exception {
        try (Session session1 = getSession(player1URI);
             Session session2 = getSession(player2URI)) {

            assertSoftly(softly -> {
                try {

                    softly.assertThat(receiveRawMessage(session1))
                            .as("session1 should be connected")
                            .isEqualTo("OKAY");

                    softly.assertThat(receiveRawMessage(session2))
                            .as("session2 should be connected")
                            .isEqualTo("OKAY");

                    softly.assertThat(waitToReceiveMessage(session1, message -> Message.Type.CONNECTED.equals(message.type())))
                            .as("session1 should have connection established")
                            .isNotNull();

                    softly.assertThat(waitToReceiveMessage(session2, message -> Message.Type.CONNECTED.equals(message.type())))
                            .as("session2 should have connection established")
                            .isNotNull();

                    send(session1, Message.Type.NEW_GAME.build());

                    softly.assertThat(waitToReceiveMessage(session1, message -> Message.Type.WAITING_PLAYERS.equals(message.type())))
                            .as("session1 game should be waiting for players")
                            .isNotNull();

                    send(session2, Message.Type.NEW_GAME.build());

                    Message gameReadyFromSession2;
                    softly.assertThat(gameReadyFromSession2 = waitToReceiveMessage(session2, message -> Message.Type.GAME_READY.equals(message.type())))
                            .as(Message.Type.GAME_READY + " received from session2")
                            .isNotNull();

                    softly.assertThat(Message.Field.opponentName.get(gameReadyFromSession2))
                            .as(" opponent of player 2 should be player 1")
                            .isEqualTo("player 1");

                    var gameIdSession2 = Message.Field.gameId.get(gameReadyFromSession2);

                    Message gameReadyFromSession1;

                    softly.assertThat(gameReadyFromSession1 = waitToReceiveMessage(session1, message -> Message.Type.GAME_READY.equals(message.type())))
                            .as(Message.Type.GAME_READY + " received from session1")
                            .isNotNull();

                    softly.assertThat(Message.Field.opponentName.get(gameReadyFromSession1))
                            .as("opponent of player 1 should be player 2")
                            .isEqualTo("player 2");

                    var gameIdSession1 = Message.Field.gameId.get(gameReadyFromSession1);

                    softly.assertThat(gameIdSession1)
                            .as("gameId should be the same")
                            .isEqualTo(gameIdSession2);


                    send(session1, Message.Type.PLAY_GAME.build(messageSetter -> {
                        messageSetter.set(Message.Field.gameId, gameIdSession1)
                                .set(Message.Field.movement, Movement.ROCK.name());
                    }));

                    softly.assertThat(waitToReceiveMessage(session1, m -> Message.Type.GAME_RUNNING.equals(m.type())))
                            .as(Message.Type.GAME_RUNNING + " received from session1")
                            .isNotNull();

                    softly.assertThat(waitToReceiveMessage(session2, m -> Message.Type.GAME_RUNNING.equals(m.type())))
                            .as(Message.Type.GAME_RUNNING + " received from session2")
                            .isNotNull();

                    send(session2, Message.Type.PLAY_GAME.build(messageSetter -> {
                        messageSetter.set(Message.Field.gameId, gameIdSession1)
                                .set(Message.Field.movement, Movement.ROCK.name());
                    }));

                    Message gameOverSession1 = waitToReceiveMessage(session1, m -> Message.Type.GAME_OVER_DRAW.equals(m.type()));
                    softly.assertThat(gameOverSession1)
                            .as(Message.Type.GAME_OVER_DRAW + " received from session2")
                            .isNotNull();

                    softly.assertThat(Message.Field.opponentName.get(gameOverSession1))
                            .as("opponent should be player 2")
                            .isEqualTo("player 2");

                    softly.assertThat(Message.Field.opponentMovement.get(gameOverSession1))
                            .as("opponent movement should be " + Movement.ROCK.name())
                            .isEqualTo(Movement.ROCK.name());


                    Message gameOverSession2 = waitToReceiveMessage(session2, m -> Message.Type.GAME_OVER_DRAW.equals(m.type()));

                    softly.assertThat(gameOverSession2)
                            .as(Message.Type.GAME_OVER_DRAW + " received from session2")
                            .isNotNull();

                    softly.assertThat(Message.Field.opponentName.get(gameOverSession2))
                            .as("opponent should be player 1")
                            .isEqualTo("player 1");

                    softly.assertThat(Message.Field.opponentMovement.get(gameOverSession2))
                            .as("opponent movement should be " + Movement.ROCK.name())
                            .isEqualTo(Movement.ROCK.name());

                    System.out.println("fim");
                } catch (InterruptedException e) {
                    softly.fail(e.getMessage(), e);
                }
            });
        }
    }


    @Test
    void shouldGameOverAbandoned() throws Exception {
        try (Session session1 = getSession(player1URI)) {

            assertSoftly(softly -> {
                try {

                    try (Session session2 = getSession(player2URI)) {

                        softly.assertThat(receiveRawMessage(session1))
                                .as("session1 should be connected")
                                .isEqualTo("OKAY");

                        softly.assertThat(receiveRawMessage(session2))
                                .as("session2 should be connected")
                                .isEqualTo("OKAY");

                        softly.assertThat(waitToReceiveMessage(session1, message -> Message.Type.CONNECTED.equals(message.type())))
                                .as("session1 should have connection established")
                                .isNotNull();

                        softly.assertThat(waitToReceiveMessage(session2, message -> Message.Type.CONNECTED.equals(message.type())))
                                .as("session2 should have connection established")
                                .isNotNull();

                        send(session1, Message.Type.NEW_GAME.build());

                        softly.assertThat(waitToReceiveMessage(session1, message -> Message.Type.WAITING_PLAYERS.equals(message.type())))
                                .as("session1 game should be waiting for players")
                                .isNotNull();

                        send(session2, Message.Type.NEW_GAME.build());

                        Message gameReadyFromSession2;
                        softly.assertThat(gameReadyFromSession2 = waitToReceiveMessage(session2, message -> Message.Type.GAME_READY.equals(message.type())))
                                .as(Message.Type.GAME_READY + " received from session2")
                                .isNotNull();

                        softly.assertThat(Message.Field.opponentName.get(gameReadyFromSession2))
                                .as(" opponent of player2 should be player 1")
                                .isEqualTo("player 1");

                        var gameIdSession2 = Message.Field.gameId.get(gameReadyFromSession2);

                        Message gameReadyFromSession1;

                        softly.assertThat(gameReadyFromSession1 = waitToReceiveMessage(session1, message -> Message.Type.GAME_READY.equals(message.type())))
                                .as(Message.Type.GAME_READY + " received from session1")
                                .isNotNull();

                        softly.assertThat(Message.Field.opponentName.get(gameReadyFromSession1))
                                .as("opponent of player 1 should be player 2")
                                .isEqualTo("player 2");

                        var gameIdSession1 = Message.Field.gameId.get(gameReadyFromSession1);

                        softly.assertThat(gameIdSession1)
                                .as("gameId should be the same")
                                .isEqualTo(gameIdSession2);

                    } catch (DeploymentException | IOException e) {
                        softly.fail(e.getMessage(), e);
                    }

                    Message gameOverAbandoned;
                    softly.assertThat(gameOverAbandoned = waitToReceiveMessage(session1, message -> Message.Type.GAME_OVER_ABANDONED.equals(message.type())))
                            .as(Message.Type.GAME_OVER_ABANDONED + " received from session2")
                            .isNotNull();

                    System.out.println("fim");
                } catch (InterruptedException e) {
                    softly.fail(e.getMessage(), e);
                }
            });
        }
    }


    private static Future<Void> send(Session session, Message message) {
        return session.getAsyncRemote().sendText(message.toJson());
    }

    private Session getSession(URI uri) throws DeploymentException, IOException {
        return ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri);
    }

    @ClientEndpoint
    public static class Client {

        private String sessionId;

        @OnOpen
        public void open(Session session) {
            this.sessionId = session.getId();
            messagesFrom(this.sessionId).add("OKAY");
        }

        @OnMessage
        void message(String msg) {
            MESSAGES.computeIfAbsent(sessionId, k -> new LinkedBlockingDeque<>()).add(msg);
        }

    }


}
