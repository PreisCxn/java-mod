package de.alive.api.networking;

import de.alive.api.networking.sockets.IWebSocketConnector;
import de.alive.api.networking.sockets.SocketMessageListener;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface IServerChecker {

    @NotNull
    Mono<Boolean> checkConnection();

    @NotNull
    Mono<Boolean> isConnected();

    void addSocketListener(SocketMessageListener listener);

    void removeSocketListener(SocketMessageListener listener);

    @NotNull
    NetworkingState getState();

    @NotNull
    Mono<String> getServerMinVersion();

    @NotNull
    IWebSocketConnector getWebsocket();

}
