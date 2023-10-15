package br.org.soujava.coffewithjava.jnopo;

import br.org.soujava.coffewithjava.jnopo.core.GameOver;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
@ClientEndpoint
public class JNopoCatcher {

    private final static Logger logger = Logger.getLogger(JNopoCatcher.class.getCanonicalName());

    private final Jsonb jsonb = JsonbBuilder.create();

    @Inject
    @ConfigProperty(name = "jnopo-game.websocket-url")
    private String url;

    private Session session;

    private void connect() {
        try {
            logger.info("connecting in...");
            URI path = URI.create(url);
            this.session = ContainerProvider.getWebSocketContainer()
                    .connectToServer(this, path);
            logger.info("connected to %s%n".formatted(path.toString()));
        } catch (DeploymentException | IOException e) {
            logger.log(Level.WARNING, "failure to connect to the server", e);
        }
    }

    @OnClose
    public void onClose() throws InterruptedException, DeploymentException, IOException {
        logger.info("session was closed...");
    }


    @OnError
    public void onError(Session session,
                        Throwable thr) {
        logger.log(Level.WARNING, "unexpected error!", thr);
    }

    @OnMessage
    @WithSpan
    public void onMessage(String message) {
        logger.info("Received the event >> %s".formatted(message));
        var event = jsonb.fromJson(message, GameEvent.class);
        save(event);
        logger.info("Converted input >> %s".formatted(event));

    }


    @Schedule(second = "*/15", hour = "*", minute = "*")
    public void checkConnection() {
        try {
            if (session == null || !session.isOpen()) {
                connect();
            }
            if (session != null && session.isOpen()) {
                this.session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[]{1}));
                logger.log(Level.INFO, "connection okay!");
            }
        } catch (IOException e) {
            try {
                this.session.close();
            } catch (IOException ex) {
                logger.log(Level.WARNING, "failure on check connection", e);
            }
            this.session = null;
        }
    }

    @Inject
    @Database(DatabaseType.DOCUMENT)
    Playoffs playoffs;

    @WithSpan
    private void save(GameEvent event) {
        var game = event.gameover();
        var match = new GameMatch(game.gameId(),
                new PlayerInfo(game.playerA().name(), game.playerAMovement()),
                new PlayerInfo(game.playerB().name(), game.playerBMovement()),
                game.isTied(),
                game.winnerInfo()
                        .map(p -> new PlayerInfo(p.player().name(), p.movement()))
                        .orElse(null),
                game.loserInfo()
                        .map(p -> new PlayerInfo(p.player().name(), p.movement()))
                        .orElse(null)
        );
        playoffs.save(match);
    }


}
