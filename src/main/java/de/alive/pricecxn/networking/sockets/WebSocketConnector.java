package de.alive.pricecxn.networking.sockets;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@ClientEndpoint
public class WebSocketConnector {
    private Session session;

    private boolean isConnected = false;

    public static final String DEFAULT_WEBSOCKET_URI = "ws://127.0.0.1:7070";

    private CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();
    private final List<SocketMessageListener> messageListeners = new CopyOnWriteArrayList<>();
    private final List<SocketCloseListener> closeListeners = new CopyOnWriteArrayList<>();
    private final List<SocketOpenListener> openListeners = new CopyOnWriteArrayList<>();

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.isConnected = true;
        connectionFuture.complete(true);
        synchronized (openListeners) {
            for (SocketOpenListener listener : openListeners) {
                listener.onOpen(session);
            }
        }
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("WebSocket message: " + message);
        synchronized (messageListeners) {
            for (SocketMessageListener listener : messageListeners) {
                listener.onMessage(message);
            }
        }
    }

    @OnClose
    public void onClose() {
        this.isConnected = false;
        connectionFuture = new CompletableFuture<>();
        synchronized (closeListeners) {
            for (SocketCloseListener listener : closeListeners) {
                listener.onClose();
            }
        }
        System.out.println("WebSocket connection closed");
    }

    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    public CompletableFuture<Boolean> connectToWebSocketServer(String serverUri) {
        try {
            System.out.println("connecting...123");
            connectionFuture = new CompletableFuture<>();
            System.out.println("test22131");
            WebSocketContainer container = null;
            try {
                container = ContainerProvider.getWebSocketContainer();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                this.isConnected = false;
                connectionFuture.complete(false);
                System.out.println("connecting1...");
            }
            System.out.println("connecting2...");
            if (container == null) {
                this.isConnected = false;
                connectionFuture.complete(false);
                System.out.println("connecting2.1...");
                return connectionFuture;
            }
            try {
                container.connectToServer(this, new URI(serverUri));
                System.out.println("connecting3...");
            } catch (Exception e) {
                System.out.println("connecting4...");
                System.err.println(e.getMessage());
                this.isConnected = false;
                connectionFuture.complete(false);
                System.out.println("connecting5...");
            }

            return connectionFuture;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            this.isConnected = false;
            connectionFuture.complete(false);
            System.out.println("connecting6...");
            return connectionFuture;
        }
    }

    public void sendMessage(String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            System.err.println(e.getMessage());
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

