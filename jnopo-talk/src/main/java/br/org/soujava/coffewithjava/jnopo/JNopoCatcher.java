package br.org.soujava.coffewithjava.jnopo;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.websocket.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
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

    @Inject
    Event<GameEvent> eventListener;

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
        logger.info("Received the event >> %s".formatted(message));
        eventListener.fire(event);
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
}
