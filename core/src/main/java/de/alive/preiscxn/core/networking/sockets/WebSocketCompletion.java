package de.alive.preiscxn.core.networking.sockets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.alive.api.networking.sockets.IWebSocketConnector;
import de.alive.api.networking.sockets.SocketMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static de.alive.api.LogPrinter.LOGGER;

public class WebSocketCompletion {
    public static final String QUERY_STRING = "pcxn?";
    private static final int DEFAULT_TIMEOUT = 5000;

    private final CompletableFuture<String> future = new CompletableFuture<>();
    private final ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(1);
    private final @NotNull IWebSocketConnector connector;

    public WebSocketCompletion(@NotNull IWebSocketConnector connector, @NotNull String query, @Nullable String... data) {
        this.connector = connector;

        SocketMessageListener listener = new SocketMessageListener() {
            @Override
            public void onMessage(@NotNull String message) {
                if (message.contains(query)) {
                    try {
                        JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                        if (json.has(query)) {
                            future.complete(json.get(query).getAsString());
                            connector.removeMessageListener(this);
                            cancelTimeout();
                        }
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                        cancelTimeout();
                    }
                }
            }
        };

        this.connector.addMessageListener(listener);
        String queryString = QUERY_STRING + query + (data == null || data.length < 1 ? "" : "&" + Arrays.toString(data).replace(" ", ""));

        LOGGER.debug(queryString);
        connector.sendMessage(queryString);

        timeoutExecutor.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Response timed out"));
                connector.removeMessageListener(listener);
            }
        }, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public @NotNull Mono<String> getMono() {
        return Mono.fromFuture(future);
    }

    private void cancelTimeout() {
        timeoutExecutor.shutdownNow();
    }
}
