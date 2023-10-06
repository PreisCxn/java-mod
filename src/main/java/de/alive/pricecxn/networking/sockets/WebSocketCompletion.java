package de.alive.pricecxn.networking.sockets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.*;

public class WebSocketCompletion{
    private static final String QUERY_STRING = "pcxn?";
    private static final int DEFAULT_TIMEOUT = 5000;

    private final CompletableFuture<String> future = new CompletableFuture<>();
    private final ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(1);
    private final WebSocketConnector connector;

    public WebSocketCompletion(@NotNull WebSocketConnector connector, @NotNull String query, @Nullable String... data) {
        this.connector = connector;

        SocketMessageListener listener = new SocketMessageListener() {
            @Override
            public void onMessage(String message) {
                if(message.contains(query)) {
                    try{
                        JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                        if(json.has(query)) {
                            future.complete(json.get(query).getAsString());
                            connector.removeMessageListener(this);
                            cancelTimeout();
                        }
                    } catch (Exception e){
                        future.completeExceptionally(e);
                        cancelTimeout();
                    }
                }
            }
        };

        this.connector.addMessageListener(listener);
        connector.sendMessage(QUERY_STRING + query + (data == null ? "" : "&" + Arrays.toString(data).replace(" ", "")));

        timeoutExecutor.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Response timed out"));
                connector.removeMessageListener(listener);
            }
        }, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<String> getFuture() {
        return future;
    }

    private void cancelTimeout() {
        timeoutExecutor.shutdownNow();
    }
}
