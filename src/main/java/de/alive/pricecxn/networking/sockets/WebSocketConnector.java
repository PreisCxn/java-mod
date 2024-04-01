package de.alive.pricecxn.networking.sockets;

import org.jetbrains.annotations.NotNull;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static de.alive.pricecxn.PriceCxnMod.LOGGER;

@ClientEndpoint
public class WebSocketConnector {

    public static final String DEFAULT_WEBSOCKET_URI = "wss://socket.preiscxn.de";
    private final List<SocketMessageListener> messageListeners = new CopyOnWriteArrayList<>();
    private final List<SocketCloseListener> closeListeners = new CopyOnWriteArrayList<>();
    private final List<SocketOpenListener> openListeners = new CopyOnWriteArrayList<>();
    private final URI uri;
    private Session session;
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();

    public WebSocketConnector() {
        this(URI.create(DEFAULT_WEBSOCKET_URI));
    }

    public WebSocketConnector(URI uri) {
        this.uri = uri;
    }

    @OnOpen
    public void onOpen(@NotNull Session session) {
        this.session = session;
        synchronized(openListeners){
            for (SocketOpenListener listener : openListeners) {
                listener.onOpen(session);
            }
        }

        // Start sending pings every 30 seconds
        disposables.add(Flux.interval(Duration.ofSeconds(30))
                .flatMap(tick -> Mono.fromCallable(() -> {
                    try {
                        session.getBasicRemote().sendPing(ByteBuffer.wrap(new byte[0]));
                    } catch (IOException e) {
                        LOGGER.error("Failed to send ping", e);
                    }
                    return null;
                }).subscribeOn(Schedulers.boundedElastic()))
                .subscribe());
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
        this.session = null;

        synchronized(closeListeners){
            for (SocketCloseListener listener : closeListeners) {
                listener.onClose();
            }
        }
        synchronized(disposables){
            for (Disposable disposable : disposables) {
                disposable.dispose();
            }
        }
        LOGGER.debug("WebSocket connection closed");
    }

    @OnError
    public void onError(Throwable throwable) {
        LOGGER.error("WebSocket error", throwable);
    }

    public @NotNull Mono<Session> establishWebSocketConnection() {
        return Mono.justOrEmpty(this.session)
                .switchIfEmpty(Mono.fromCallable(() -> {
                    try{
                        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                        return container.connectToServer(this, this.uri);
                    }catch(Exception e){
                        LOGGER.error("Failed to connect to WebSocket server", e);
                        return null;
                    }
                }).subscribeOn(Schedulers.boundedElastic()));
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

    public Mono<Boolean> isConnected() {
        return Mono.justOrEmpty(this.session)
                .flatMap(session -> Mono.just(session.isOpen()))
                .switchIfEmpty(Mono.just(false));
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

