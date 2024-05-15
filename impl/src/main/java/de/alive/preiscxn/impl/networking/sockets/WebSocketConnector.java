package de.alive.preiscxn.impl.networking.sockets;

import de.alive.preiscxn.api.networking.sockets.IWebSocketConnector;
import de.alive.preiscxn.api.networking.sockets.SocketCloseListener;
import de.alive.preiscxn.api.networking.sockets.SocketMessageListener;
import de.alive.preiscxn.api.networking.sockets.SocketOpenListener;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static de.alive.preiscxn.api.LogPrinter.LOGGER;

@ClientEndpoint
public class WebSocketConnector implements IWebSocketConnector {

    private final List<SocketMessageListener> messageListeners = new CopyOnWriteArrayList<>();
    private final List<SocketCloseListener> closeListeners = new CopyOnWriteArrayList<>();
    private final List<SocketOpenListener> openListeners = new CopyOnWriteArrayList<>();
    private final URI uri;
    private @Nullable Session session;
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
        synchronized (openListeners) {
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
        synchronized (messageListeners) {
            for (SocketMessageListener listener : messageListeners) {
                listener.onMessage(message);
            }
        }
    }

    @OnClose
    public void onClose() {
        this.session = null;

        synchronized (closeListeners) {
            for (SocketCloseListener listener : closeListeners) {
                listener.onClose();
            }
        }
        synchronized (disposables) {
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
                    try {
                        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                        return container.connectToServer(this, this.uri);
                    } catch (Exception e) {
                        LOGGER.error("Failed to connect to WebSocket server", e);
                        return null;
                    }
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    public void sendMessage(String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            LOGGER.error("Failed to send message", e);
        }
    }

    public void closeWebSocket() {
        try {
            session.close();
        } catch (IOException e) {
            LOGGER.error("Failed to close WebSocket", e);
        }
    }

    @Override
    public boolean isConnected() {
        if (this.session == null) {
            return false;
        }
        return this.session.isOpen();
    }

    @Override
    public void addMessageListener(SocketMessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.add(listener);
        }
    }

    @Override
    public void addCloseListener(SocketCloseListener listener) {
        synchronized (closeListeners) {
            closeListeners.add(listener);
        }
    }

    @Override
    public void addOpenListener(SocketOpenListener listener) {
        synchronized (openListeners) {
            openListeners.add(listener);
        }
    }

    @Override
    public void removeMessageListener(SocketMessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.remove(listener);
        }
    }

    @Override
    public void removeCloseListener(SocketCloseListener listener) {
        synchronized (closeListeners) {
            closeListeners.remove(listener);
        }
    }

    @Override
    public void removeOpenListener(SocketOpenListener listener) {
        synchronized (openListeners) {
            openListeners.remove(listener);
        }
    }

}

