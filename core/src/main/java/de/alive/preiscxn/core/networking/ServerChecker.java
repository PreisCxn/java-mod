package de.alive.preiscxn.core.networking;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.alive.api.networking.IServerChecker;
import de.alive.api.networking.NetworkingState;
import de.alive.api.networking.sockets.SocketMessageListener;
import de.alive.preiscxn.core.networking.sockets.WebSocketCompletion;
import de.alive.preiscxn.core.networking.sockets.WebSocketConnector;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class ServerChecker implements IServerChecker {

    private final @NotNull WebSocketConnector websocket;

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
    @Override
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
    @Override
    public @NotNull Mono<Boolean> isConnected() {
        if (!this.websocket.isConnected())
            return checkConnection();
        return Mono.just(true);
    }

    @Override
    public void addSocketListener(SocketMessageListener listener) {
        this.websocket.addMessageListener(listener);
    }

    @Override
    public void removeSocketListener(SocketMessageListener listener) {
        this.websocket.removeMessageListener(listener);
    }

    @Override
    public @NotNull NetworkingState getState() {
        return state;
    }

    @Override
    public @NotNull Mono<String> getServerMinVersion() {
        return minVersion;
    }

    @Override
    public @NotNull WebSocketConnector getWebsocket() {
        return websocket;
    }

    private void onWebsocketMessage(@NotNull String message) {
        try {
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

        } catch (JsonSyntaxException ignored) {
            connectionFuture.complete(state != NetworkingState.OFFLINE);
        }
    }

}
