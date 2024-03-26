package de.alive.pricecxn.networking;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.alive.pricecxn.networking.sockets.SocketMessageListener;
import de.alive.pricecxn.networking.sockets.WebSocketCompletion;
import de.alive.pricecxn.networking.sockets.WebSocketConnector;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ServerChecker {

    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private final WebSocketConnector websocket;

    private final CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();
    private final CompletableFuture<Boolean> maintenanceFuture = new CompletableFuture<>();
    private final CompletableFuture<String> minVersionFuture = new CompletableFuture<>();
    private final Mono<String> minVersion = Mono.fromFuture(minVersionFuture);
    private @NotNull NetworkingState state = NetworkingState.OFFLINE;

    /**
     * This constructor is used to check if the server is reachable
     * Uses the default check interval and uri
     */
    public ServerChecker() {
        this(WebSocketConnector.DEFAULT_WEBSOCKET_URI);
    }

    /**
     * This constructor is used to check if the server is reachable
     *
     * @param uri The uri of the server
     */
    public ServerChecker(@NotNull String uri) {
        this.websocket = new WebSocketConnector(URI.create(uri));

        this.websocket.addMessageListener(this::onWebsocketMessage);
        this.websocket.addCloseListener(() -> this.state = NetworkingState.OFFLINE);

        Mono<Boolean> maintenance = Mono.fromFuture(maintenanceFuture);
        Mono.zip(minVersion, maintenance)
                .doOnNext(objects -> connectionFuture.complete(state != NetworkingState.OFFLINE))
                .subscribe();
    }

    /**
     * This method is used to check if the server is reachable
     *
     * @return A CompletableFuture which returns true if the server is reachable and false if not
     */
    public @NotNull Mono<Boolean> checkConnection() {
        return this.websocket.establishWebSocketConnection()
                .hasElement()
                .onErrorResume(throwable -> {
                    this.state = NetworkingState.OFFLINE;
                    connectionFuture.complete(false);
                    return Mono.just(false);
                })
                .doOnNext(isConnected -> {
                    if (isConnected) {
                        this.websocket.sendMessage(WebSocketCompletion.QUERY_STRING + "maintenance");
                        this.websocket.sendMessage(WebSocketCompletion.QUERY_STRING + "min-version");
                    } else {
                        connectionFuture.complete(false);
                    }
                });
    }

    /**
     * This method is used to check if the server is reachable
     * Only checks if the server is reachable if the last check was more than the check interval ago or the last check was never
     *
     * @return A CompletableFuture which returns true if the server is reachable and false if not
     */
    public Mono<Boolean> isConnected() {
        return this.websocket
                .isConnected()
                .flatMap(aBoolean -> {
                    if (!aBoolean)
                        return checkConnection();

                    return Mono.just(true);
                });
    }

    public void addSocketListener(SocketMessageListener listener) {
        this.websocket.addMessageListener(listener);
    }

    public void removeSocketListener(SocketMessageListener listener) {
        this.websocket.removeMessageListener(listener);
    }

    public @NotNull NetworkingState getState() {
        return state;
    }

    public @NotNull Mono<String> getServerMinVersion() {
        return minVersion;
    }

    public @NotNull WebSocketConnector getWebsocket() {
        return websocket;
    }

    private void onWebsocketMessage(@NotNull String message) {
        try{
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has("min-version")) {
                this.minVersionFuture.complete(json.get("min-version").getAsString());
            }

            if (json.has("maintenance")) {
                if (json.get("maintenance").getAsBoolean())
                    this.state = NetworkingState.MAINTENANCE;
                else
                    this.state = NetworkingState.ONLINE;
                this.maintenanceFuture.complete(true);
            }

        }catch(JsonSyntaxException ignored){
            connectionFuture.complete(state != NetworkingState.OFFLINE);
        }
    }

}
