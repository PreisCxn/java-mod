package de.alive.pricecxn.networking.sockets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.alive.pricecxn.PriceCxnMod.LOGGER;

@ClientEndpoint
public class WebSocketConnector {

    public static final String DEFAULT_WEBSOCKET_URI = "wss://socket.preiscxn.de";
    private final List<SocketMessageListener> messageListeners = new CopyOnWriteArrayList<>();
    private final List<SocketCloseListener> closeListeners = new CopyOnWriteArrayList<>();
    private final List<SocketOpenListener> openListeners = new CopyOnWriteArrayList<>();
    private final CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();
    private Session session;
    private boolean isConnected = false;
    private ScheduledExecutorService pingExecutor;
    private @Nullable Boolean isConnectionEstablished = null;

    @OnOpen
    public void onOpen(@NotNull Session session) {
        this.session = session;
        this.isConnected = true;
        isConnectionEstablished = true;
        synchronized(openListeners){
            for (SocketOpenListener listener : openListeners) {
                listener.onOpen(session);
            }
        }

        // Start sending pings every 30 seconds
        pingExecutor = Executors.newSingleThreadScheduledExecutor();
        pingExecutor.scheduleAtFixedRate(() -> {
            try{
                session.getBasicRemote().sendPing(ByteBuffer.wrap(new byte[0]));
            }catch(IOException e){
                LOGGER.error("Failed to send ping", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @OnMessage
    public void onMessage(String message) {
        LOGGER.debug("WebSocket message: " + message);
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
        LOGGER.debug("WebSocket connection closed");
    }

    @OnError
    public void onError(Throwable throwable) {
        LOGGER.error("WebSocket error", throwable);
    }

    public @NotNull Mono<Boolean> connectToWebSocketServer(@NotNull String serverUri) {
        return Mono.fromCallable(() -> {
            try {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                container.connectToServer(this, new URI(serverUri));
                return true;
            } catch (Exception e) {
                LOGGER.error("Failed to connect to WebSocket server", e);
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public void sendMessage(String message) {
        try{
            session.getBasicRemote().sendText(message);
        }catch(IOException e){
            LOGGER.error("Failed to send message", e);
        }
    }

    public void closeWebSocket() {
        try{
            session.close();
        }catch(IOException e){
            LOGGER.error("Failed to close WebSocket", e);
        }
    }

    public boolean getIsConnected() {
        return this.isConnected;
    }

    public void addMessageListener(SocketMessageListener listener) {
        synchronized(messageListeners){
            messageListeners.add(listener);
        }
    }

    public void addCloseListener(SocketCloseListener listener) {
        synchronized(closeListeners){
            closeListeners.add(listener);
        }
    }

    public void addOpenListener(SocketOpenListener listener) {
        synchronized(openListeners){
            openListeners.add(listener);
        }
    }

    public void removeMessageListener(SocketMessageListener listener) {
        synchronized(messageListeners){
            messageListeners.remove(listener);
        }
    }

    public void removeCloseListener(SocketCloseListener listener) {
        synchronized(closeListeners){
            closeListeners.remove(listener);
        }
    }

    public void removeOpenListener(SocketOpenListener listener) {
        synchronized(openListeners){
            openListeners.remove(listener);
        }
    }

}

