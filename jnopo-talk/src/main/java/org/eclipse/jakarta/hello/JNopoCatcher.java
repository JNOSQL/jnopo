package org.eclipse.jakarta.hello;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
@ClientEndpoint
public class JNopoCatcher {

    private final static Logger logger = Logger.getLogger(JNopoCatcher.class.getCanonicalName());

    private Session session;

    private void connect() {
        try {
            logger.info("connecting in...");
            URI path = URI.create("ws://jnopo-game-dearrudam-2-dev.apps.sandbox-m2.ll9k.p1.openshiftapps.com/jnopo-catch/jnopo-talk");
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

    @Schedule(second = "*/15", hour = "*", minute = "*")
    public void checkConnection() {
        try {
            if (session == null || !session.isOpen()) {
                connect();
            }
            if (session != null || session.isOpen()) {
                this.session.getBasicRemote().sendPing(ByteBuffer.wrap(new byte[]{1}));
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

    @OnMessage
    public void onMessage(String message) {
        System.out.println(message);

    }
}
