package de.alive.pricecxn.networking.sockets;

import reactor.core.publisher.Mono;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@ClientEndpoint
public class WebSocketConnector {

    public static final String DEFAULT_WEBSOCKET_URI = "wss://socket.preiscxn.de";
    private static final Logger LOGGER = Logger.getLogger(WebSocketConnector.class.getName());
    private CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();
    private final List<SocketMessageListener> messageListeners = new CopyOnWriteArrayList<>();
    private final List<SocketCloseListener> closeListeners = new CopyOnWriteArrayList<>();
    private final List<SocketOpenListener> openListeners = new CopyOnWriteArrayList<>();
    private Session session;
    private boolean isConnected = false;
    private ScheduledExecutorService pingExecutor;
    private Boolean isConnectionEstablished = null;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.isConnected = true;
        isConnectionEstablished = true;
        synchronized (openListeners) {
            for (SocketOpenListener listener : openListeners) {
                listener.onOpen(session);
            }
        }

        // Start sending pings every 30 seconds
        pingExecutor = Executors.newSingleThreadScheduledExecutor();
        pingExecutor.scheduleAtFixedRate(() -> {
            try {
                session.getBasicRemote().sendPing(ByteBuffer.wrap(new byte[0]));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to send ping", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @OnMessage
    public void onMessage(String message) {
        LOGGER.log(Level.FINE, "WebSocket message: " + message);
        synchronized(messageListeners){
            for (SocketMessageListener listener : messageListeners) {
                listener.onMessage(message);
            }
        }
    }

    @OnClose
    public void onClose() {
        this.isConnected = false;
        isConnectionEstablished = null;
        synchronized(closeListeners){
            for (SocketCloseListener listener : closeListeners) {
                listener.onClose();
            }
        }
        LOGGER.log(Level.FINEST, "WebSocket connection closed");
    }

    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    public CompletableFuture<Boolean> connectToWebSocketServer(String serverUri) {
        try {

            connectionFuture = new CompletableFuture<>();

            WebSocketContainer container = null;
            try {
                container = ContainerProvider.getWebSocketContainer();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                this.isConnected = false;
                connectionFuture.complete(false);
            }
            if (container == null) {
                this.isConnected = false;
                connectionFuture.complete(false);
                return connectionFuture;
            }
            try {
                container.connectToServer(this, new URI(serverUri));
            } catch (Exception e) {
                System.err.println(e.getMessage());
                this.isConnected = false;
                connectionFuture.complete(false);
            }

            return connectionFuture;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            this.isConnected = false;
            connectionFuture.complete(false);
            return connectionFuture;
        }
    }

    public void sendMessage(String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to send message", e);
        }
    }

    public void closeWebSocket() {
        try {
            session.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean getIsConnected() {
        return this.isConnected;
    }

    public void addMessageListener(SocketMessageListener listener) {
        synchronized (messageListeners){
            messageListeners.add(listener);
        }
    }

    public void addCloseListener(SocketCloseListener listener) {
        synchronized (closeListeners) {
            closeListeners.add(listener);
        }
    }

    public void addOpenListener(SocketOpenListener listener) {
        synchronized (openListeners) {
            openListeners.add(listener);
        }
    }

    public void removeMessageListener(SocketMessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.remove(listener);
        }
    }

    public void removeCloseListener(SocketCloseListener listener) {
        synchronized (closeListeners){
            closeListeners.remove(listener);
        }
    }

    public void removeOpenListener(SocketOpenListener listener) {
        synchronized (openListeners) {
            openListeners.remove(listener);
        }
    }

}

