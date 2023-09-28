package de.alive.pricecxn.networking;

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
    private List<SocketListener> messageListeners = new ArrayList<>();

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.isConnected = true;
        connectionFuture.complete(true);
    }

    @OnMessage
    public void onMessage(String message){
        for (SocketListener listener : messageListeners) {
            listener.onMessage(message);
        }
    }

    @OnClose
    public void onClose() {
        this.isConnected = false;
        connectionFuture = new CompletableFuture<>();
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

    public void addMessageListener(SocketListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(SocketListener listener) {
        messageListeners.remove(listener);
    }

}

