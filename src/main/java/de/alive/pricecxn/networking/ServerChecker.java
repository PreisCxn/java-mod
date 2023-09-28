package de.alive.pricecxn.networking;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ServerChecker {

    private static final String DEFAULT_CHECK_URI = "ws://localhost:8080";
    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final int DEFAULT_CHECK_INTERVAL = 60000;
    private boolean connected = false;
    private final String uri;
    private final int checkInterval;
    private long lastCheck = 0;
    private final WebSocketConnector websocket = new WebSocketConnector();

    /**
     * This constructor is used to check if the server is reachable
     *
     * @param uri           The uri of the server
     * @param checkInterval The interval in which the server is checked in milliseconds
     */
    public ServerChecker(@Nullable String uri, int checkInterval) {
        this.uri = uri == null ? DEFAULT_CHECK_URI : uri;
        this.checkInterval = checkInterval < 0 ? DEFAULT_CHECK_INTERVAL : checkInterval;
        checkConnection();
    }

    /**
     * This constructor is used to check if the server is reachable
     * Uses the default check interval and uri
     */
    public ServerChecker() {
        this(null, -1);
    }

    /**
     * This method is used to check if the server is reachable
     * @return A CompletableFuture which returns true if the server is reachable and false if not
     */
    public CompletableFuture<Boolean> checkConnection() {
        return this.websocket.connectToWebSocketServer(this.uri);
    }

    /**
     * This method is used to check if the server is reachable
     * Only checks if the server is reachable if the last check was more than the check interval ago or the last check was never
     * @return A CompletableFuture which returns true if the server is reachable and false if not
     */
    public CompletableFuture<Boolean> isConnected() {
        if (this.websocket.getIsConnected())
            return CompletableFuture.completedFuture(true);
        else if(this.lastCheck == 0 || System.currentTimeMillis() - this.lastCheck > this.checkInterval)
            return checkConnection();
        else return CompletableFuture.completedFuture(false);
    }

    public void addSocketListener(SocketListener listener) {
        this.websocket.addMessageListener(listener);
    }

    public void removeSocketListener(SocketListener listener) {
        this.websocket.removeMessageListener(listener);
    }

}
