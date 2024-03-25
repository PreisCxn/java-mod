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

    public final String apiUrl;

    private final @NotNull HttpClient client = HttpClient.newHttpClient();

    private static final Http INSTANCE = new Http();

    protected Http() {
        apiUrl = "https://api.preiscxn.de/api";
    }

    public Http(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public static Http getInstance() {
        return INSTANCE;
    }

    public Mono<HttpResponse<String>> sendAsync(HttpRequest request) {
        return Mono.fromFuture(client.sendAsync(request, HttpResponse.BodyHandlers.ofString(), Http::applyPushPromise));
    }

    public <T, R> @NotNull Mono<R> GET(String uri, @NotNull Function<String, T> stringTFunction, @NotNull Function<T, R> callback, String... headers) {
        return GET(apiUrl, uri, stringTFunction, callback, headers);
    }

    public <T, R> @NotNull Mono<R> GET(String baseUri, String uri, @NotNull Function<String, T> stringTFunction, @NotNull Function<T, R> callback, String @NotNull ... headers) {
        HttpRequest.Builder get = HttpRequest.newBuilder()
                .uri(URI.create(baseUri + uri))
                .GET();

        if (headers.length > 0)
            get = get.headers(headers);

        return sendAsync(get.build())
                .map(response -> Tuples.of(response.statusCode(), response.body()))
                .handle((tuple, sink) -> {
                    if (tuple.getT1() >= 200 && tuple.getT1() < 300) {
                        T apply = stringTFunction.apply(tuple.getT2());
                        if (apply != null)
                            sink.next(callback.apply(apply));

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

    public <T, R> @NotNull Mono<R> POST(@NotNull String uri, @Nullable JsonObject json, @Nullable Function<String, T> stringTFunction, @Nullable Function<T, R> callback, @NotNull String @NotNull ... headers) {
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
                        if(stringTFunction == null || callback == null){
                            sink.complete();
                            return;
                        }

                        T apply = stringTFunction.apply(tuple.getT2());
                        if (apply != null)
                            sink.next(callback.apply(apply));

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

