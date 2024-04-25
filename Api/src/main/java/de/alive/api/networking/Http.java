package de.alive.api.networking;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;

public class Http {

    protected static final String DEFAULT_API_URL = "https://api.preiscxn.de/api";
    private static final Http INSTANCE = new Http();
    public final String apiUrl;
    private final @NotNull HttpClient client = HttpClient.newHttpClient();

    protected Http() {
        apiUrl = DEFAULT_API_URL;
    }

    public Http(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public static Http getInstance() {
        return INSTANCE;
    }

    private static <T> void applyPushPromise(HttpRequest initiatingRequest, HttpRequest pushPromiseRequest, Function<HttpResponse.BodyHandler<T>, CompletableFuture<HttpResponse<T>>> acceptor) { }

    public String getApiUrl() {
        return apiUrl;
    }

    protected @NotNull Mono<HttpResponse<byte[]>> sendAsync(HttpRequest request) {
        return Mono.fromFuture(client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray(), Http::applyPushPromise));
    }

    public Mono<String> GET(String uri) {
        return GET(DEFAULT_API_URL, uri);
    }

    public @NotNull Mono<String> GET(String baseUri, String uri) {
        HttpRequest.Builder get = HttpRequest.newBuilder()
                .uri(URI.create(baseUri + uri))
                .GET();

        return sendAsync(get.build())
                .map(response -> Tuples.of(response.statusCode(), new String(response.body())))
                .handle((tuple, sink) -> {
                    if (tuple.getT1() >= 200 && tuple.getT1() < 399) {
                        sink.next(tuple.getT2());
                        sink.complete();
                    } else {
                        String errorMessage = "Received wrong success code: " + tuple.getT1() + "(" + baseUri + uri + ")";
                        getLogger().severe(errorMessage);
                        sink.error(new IllegalStateException(tuple.getT2()));
                    }
                });
    }

    public @NotNull Mono<String> POST(@NotNull String uri, @Nullable JsonObject json) {
        HttpRequest.Builder post = HttpRequest
                .newBuilder()
                .uri(URI.create(apiUrl + uri));

        if (json == null)
            post = post.POST(HttpRequest.BodyPublishers.noBody());
        else
            post = post.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()));

        return sendAsync(post.build())
                .map(response -> Tuples.of(response.statusCode(), new String(response.body())))
                .handle((tuple, sink) -> {
                    if (tuple.getT1() >= 200 && tuple.getT1() < 300) {
                        sink.next(tuple.getT2());
                        sink.complete();
                    } else {
                        String errorMessage = "Received wrong success code: " + tuple.getT1() + "(" + uri + ")";
                        getLogger().severe(errorMessage);
                        sink.error(new IllegalStateException(tuple.getT2()));
                    }
                });
    }

    private Logger getLogger() {
        return Logger.getLogger(Http.class.getName());
    }

    public Mono<byte[]> getBytes(String baseUrl, String s) {
        HttpRequest.Builder get = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + s))
                .GET();

        return sendAsync(get.build())
                .map(response -> Tuples.of(response.statusCode(), response.body()))
                .handle((tuple, sink) -> {
                    if (tuple.getT1() >= 200 && tuple.getT1() < 399) {
                        sink.next(tuple.getT2());
                        sink.complete();
                    } else {
                        String errorMessage = "Received wrong success code: " + tuple.getT1() + "(" + baseUrl + s + ")";
                        getLogger().severe(errorMessage);
                        sink.error(new IllegalStateException(new String(tuple.getT2())));
                    }
                });
    }

}

