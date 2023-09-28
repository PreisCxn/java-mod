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

@ClientEndpoint
public class WebSocketConnector {
    private Session session;

    private boolean isConnected = false;

    private CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();
    private final List<SocketMessageListener> messageListeners = new ArrayList<>();
    private final List<SocketCloseListener> closeListeners = new ArrayList<>();
    private final List<SocketOpenListener> openListeners = new ArrayList<>();

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.isConnected = true;
        connectionFuture.complete(true);
        for (SocketOpenListener listener : openListeners) {
            listener.onOpen(session);
        }
    }

    @OnMessage
    public void onMessage(String message){
        for (SocketMessageListener listener : messageListeners) {
            listener.onMessage(message);
        }
    }

    @OnClose
    public void onClose() {
        this.isConnected = false;
        connectionFuture = new CompletableFuture<>();
        for (SocketCloseListener listener : closeListeners) {
            listener.onClose();
        }
        System.out.println("WebSocket connection closed");
    }

    @OnError
    public void onError(Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    public CompletableFuture<Boolean> connectToWebSocketServer(String serverUri) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, new URI(serverUri));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            this.isConnected = false;
            connectionFuture.complete(false);
        }

        return connectionFuture;
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
        messageListeners.add(listener);
    }

    public void addCloseListener(SocketCloseListener listener) {
        closeListeners.add(listener);
    }

    public void addOpenListener(SocketOpenListener listener) {
        openListeners.add(listener);
    }

    public void removeMessageListener(SocketMessageListener listener) {
        messageListeners.remove(listener);
    }

    public void removeCloseListener(SocketCloseListener listener) {
        closeListeners.remove(listener);
    }

    public void removeOpenListener(SocketOpenListener listener) {
        openListeners.remove(listener);
    }

}

