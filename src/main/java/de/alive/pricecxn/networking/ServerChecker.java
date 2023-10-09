package de.alive.pricecxn.networking;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.alive.pricecxn.networking.sockets.SocketMessageListener;
import de.alive.pricecxn.networking.sockets.WebSocketConnector;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ServerChecker {

    private static final String DEFAULT_CHECK_URI = "ws://127.0.0.1:8080";
    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final int DEFAULT_CHECK_INTERVAL = 1; //todo: change back up to 300000
    private boolean connected = false;
    private final String uri;
    private final int checkInterval;
    private long lastCheck = 0;
    private final WebSocketConnector websocket = new WebSocketConnector();

    private CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();
    private CompletableFuture<Boolean> maintenanceFuture = new CompletableFuture<>();
    private CompletableFuture<Void> minVersionFuture = new CompletableFuture<>();

    private NetworkingState state = NetworkingState.OFFLINE;

    private String serverMinVersion = null;



    /**
     * This constructor is used to check if the server is reachable
     *
     * @param uri           The uri of the server
     * @param checkInterval The interval in which the server is checked in milliseconds
     */
    public ServerChecker(@Nullable String uri, int checkInterval) {
        this.uri = uri == null ? DEFAULT_CHECK_URI : uri;
        this.checkInterval = checkInterval < 0 ? DEFAULT_CHECK_INTERVAL : checkInterval;

        this.websocket.addMessageListener(message -> {
            try{
                JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                if(json.has("min-version")) {
                    this.serverMinVersion = json.get("min-version").getAsString();
                    this.minVersionFuture.complete(null);
                }

                if(json.has("maintenance")) {
                    if(json.get("maintenance").getAsBoolean())
                        this.state = NetworkingState.MAINTENANCE;
                    else
                        this.state = NetworkingState.ONLINE;
                    this.maintenanceFuture.complete(true);
                }

                minVersionFuture.thenRun(() -> {
                    maintenanceFuture.thenRun(() -> {
                        connectionFuture.complete(state != NetworkingState.OFFLINE);
                    });
                });

            } catch (JsonSyntaxException ignored){
                connectionFuture.complete(state != NetworkingState.OFFLINE);
            }
        });
        this.websocket.addCloseListener(() -> this.state = NetworkingState.OFFLINE);

        //checkConnection();
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
        System.out.println("checking connection websocket");
        connectionFuture = new CompletableFuture<>();
        maintenanceFuture = new CompletableFuture<>();
        minVersionFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> future = this.websocket.connectToWebSocketServer(this.uri).exceptionally(throwable -> {
            System.out.println("websocket connection failed");
            this.state = NetworkingState.OFFLINE;
            connectionFuture.complete(false);
            return false;
        });

        System.out.println("checking connection websocket2");

        future.thenCompose(isConnected -> {
            System.out.println("checking connection websocket3");
            if(isConnected) {
                System.out.println("websocket connected");
                this.websocket.sendMessage("pcxn?maintenance");
                this.websocket.sendMessage("pcxn?min-version");
            } else {
                System.out.println("websocket not connected");
                connectionFuture.complete(false);
            }
            System.out.println("checking connection websocket4");
            return null;
        });

        return connectionFuture;
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

    public void addSocketListener(SocketMessageListener listener) {
        this.websocket.addMessageListener(listener);
    }

    public void removeSocketListener(SocketMessageListener listener) {
        this.websocket.removeMessageListener(listener);
    }

    public NetworkingState getState() {
        return state;
    }

    public String getServerMinVersion() {
        return serverMinVersion;
    }

    public WebSocketConnector getWebsocket() {
        return websocket;
    }
}
