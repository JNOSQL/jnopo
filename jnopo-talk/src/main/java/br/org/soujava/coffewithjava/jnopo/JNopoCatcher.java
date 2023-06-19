package br.org.soujava.coffewithjava.jnopo;

import br.org.soujava.coffewithjava.jnopo.core.GameOver;
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

    @Inject
    private Playoffs playoffs;

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
    public void onMessage(String message) {
        var event = jsonb.fromJson(message, GameEvent.class);
        GameOver gameover = event.gameover();
        playoffs.save(new GameMatch(
                gameover.gameId(),
                gameover.isTied(),
                new PlayerInfo(new Player(gameover.playerA().name()), gameover.playerAMovement()),
                new PlayerInfo(new Player(gameover.playerA().name()), gameover.playerBMovement()),
                new WinnerInfo(gameover.winner()
                        .map(p -> new Player(p.name()))
                        .orElse(null), gameover.winnerMovement().orElse(null)),
                new LoserInfo(
                        gameover.loser()
                                .map(p -> new Player(p.name()))
                                .orElse(null),
                        gameover.loserMovement().orElse(null))
        ));
        System.out.println("Salvou!!");
    }

    @Schedule(second = "*/15", hour = "*", minute = "*")
    public void checkConnection() {
        try {
            if (session == null || !session.isOpen()) {
                connect();
            }
            if (session != null || session.isOpen()) {
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
}
