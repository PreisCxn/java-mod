package de.alive.pricecxn.networking;

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
    public final String apiUrl;

    private final @NotNull HttpClient client = HttpClient.newHttpClient();

    private static final Http INSTANCE = new Http();

    protected Http() {
        apiUrl = DEFAULT_API_URL;
    }

    public Http(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public static Http getInstance() {
        return INSTANCE;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    protected Mono<HttpResponse<String>> sendAsync(HttpRequest request) {
        return Mono.fromFuture(client.sendAsync(request, HttpResponse.BodyHandlers.ofString(), Http::applyPushPromise));
    }

    public <T> @NotNull Mono<T> GET(String uri, @NotNull Function<String, T> stringTFunction, String... headers) {
        return GET(apiUrl, uri, stringTFunction, headers);
    }

    public <T> @NotNull Mono<T> GET(String baseUri, String uri, @NotNull Function<String, T> stringTFunction, String @NotNull ... headers) {
        HttpRequest.Builder get = HttpRequest.newBuilder()
                .uri(URI.create(baseUri + uri))
                .GET();

        if (headers.length > 0)
            get = get.headers(headers);

        return sendAsync(get.build())
                .map(response -> Tuples.of(response.statusCode(), response.body()))
                .handle((tuple, sink) -> {
                    if (tuple.getT1() >= 200 && tuple.getT1() < 300) {
                        sink.next(stringTFunction.apply(tuple.getT2()));
                        sink.complete();
                    } else {
                        String errorMessage = "Received wrong success code: " + tuple.getT1() + "(" + baseUri + uri + ")";
                        getLogger().severe(errorMessage);
                        sink.error(new IllegalStateException(tuple.getT2()));
                    }
                });
    }

    public @NotNull Mono<Void> POST(@NotNull String uri, @Nullable JsonObject json) {
        return POST(uri, json, null, null).then();
    }

    public <T> @NotNull Mono<T> POST(@NotNull String uri, @Nullable JsonObject json, @Nullable Function<String, T> stringTFunction, @NotNull String @NotNull ... headers) {
        HttpRequest.Builder post = HttpRequest
                .newBuilder()
                .uri(URI.create(apiUrl + uri));

        if (headers.length > 0)
            post = post.headers(headers);

        if (json == null)
            post = post.POST(HttpRequest.BodyPublishers.noBody());
        else
            post = post.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()));

        return sendAsync(post.build())
                .map(response -> Tuples.of(response.statusCode(), response.body()))
                .handle((tuple, sink) -> {
                    if (tuple.getT1() >= 200 && tuple.getT1() < 300) {
                        if(stringTFunction == null){
                            sink.complete();
                            return;
                        }
                        sink.next(stringTFunction.apply(tuple.getT2()));

                        sink.complete();
                    } else {
                        String errorMessage = "Received wrong success code: " + tuple.getT1() + "(" + uri + ")";
                        getLogger().severe(errorMessage);
                        sink.error(new IllegalStateException(tuple.getT2()));
                    }
                });
    }

    private Logger getLogger(){
        return Logger.getLogger(Http.class.getName());
    }

    private static <T> void applyPushPromise(HttpRequest initiatingRequest, HttpRequest pushPromiseRequest, Function<HttpResponse.BodyHandler<T>, CompletableFuture<HttpResponse<T>>> acceptor) { }

}

