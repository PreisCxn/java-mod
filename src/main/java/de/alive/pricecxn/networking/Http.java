package de.alive.pricecxn.networking;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.Contract;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Http {

    public static final String API_URL = "https://api.preiscxn.de/api";

    private static final @NotNull HttpClient client = HttpClient.newHttpClient();

    public static <T, R> @NotNull Mono<R> GET(String uri, @NotNull Function<String, T> stringTFunction, @NotNull Function<T, R> callback, String... headers) {
        return GET(API_URL, uri, stringTFunction, callback, headers);
    }

    public static <T, R> @NotNull Mono<R> GET(String baseUri, String uri, @NotNull Function<String, T> stringTFunction, @NotNull Function<T, R> callback, String @NotNull ... headers) {
        HttpRequest.Builder get = HttpRequest.newBuilder()
                .uri(URI.create(baseUri + uri))
                .GET();

        if (headers.length > 0)
            get = get.headers(headers);

        return Mono.fromFuture(client.sendAsync(get.build(), HttpResponse.BodyHandlers.ofString(), Http::applyPushPromise))
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

    public static @NotNull Mono<Void> PUT(@NotNull String uri, @Nullable JsonObject json, @NotNull String... headers) {
        return Mono.fromFuture(
                        client.sendAsync(
                                buildPutBuilder(uri, json, headers)
                                        .build(), HttpResponse.BodyHandlers.ofString(),
                                Http::applyPushPromise))
                .flatMap(response -> {
                    int statusCode = response.statusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        return Mono.empty();
                    } else {
                        String errorMessage = "PUT request to " + uri + " failed with status code: " + statusCode;
                        getLogger().severe(errorMessage);
                        return Mono.error(new IllegalStateException(response.body()));
                    }
                });
    }

    public static <T, R> @NotNull Mono<R> PUT(@NotNull String uri, @Nullable JsonObject json, @NotNull Function<String, T> stringTFunction, @NotNull Function<T, R> callback, @NotNull String... headers) {
        HttpRequest.Builder putBuilder = buildPutBuilder(uri, json, headers);
        return Mono
                .fromFuture(client.sendAsync(putBuilder.build(), HttpResponse.BodyHandlers.ofString(), Http::applyPushPromise))
                .handle((response, sink) -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        T apply = stringTFunction.apply(response.body());
                        if (apply != null) {
                            sink.next(callback.apply(apply));
                        } else {
                            getLogger().warning("Unable to apply callback function");
                        }
                        sink.complete();
                    } else {
                        String errorMessage = "Received wrong success code: " + response.statusCode() + "(" + uri + ")";
                        getLogger().severe(errorMessage);
                        sink.error(new IllegalStateException(response.body()));
                    }
                });
    }

    private static HttpRequest.Builder buildPutBuilder(@NotNull String uri, @Nullable JsonObject json, @NotNull String @NotNull ... headers) {
        HttpRequest.Builder put = HttpRequest
                .newBuilder()
                .uri(URI.create(API_URL + uri));

        if (headers.length > 0)
            put = put.headers(headers);

        if (json == null)
            put = put.PUT(HttpRequest.BodyPublishers.noBody());
        else
            put = put.header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json.toString()));

        return put;
    }

    public static @Nullable JsonObject jsonObjectConverter(@NotNull String s) {
        try{
            return JsonParser.parseString(s).getAsJsonObject();
        }catch(JsonSyntaxException e){
            getLogger().log(Level.WARNING, "Could not convert " + s + " to JSONObject.", e);
            return null;
        }
    }

    @Contract("_ -> new")
    public static @Nullable JsonArray jsonArrayConverter(@NotNull String s) {
        try{
            return JsonParser.parseString(s).getAsJsonArray();
        }catch(JsonSyntaxException e){
            getLogger().log(Level.WARNING, "Could not convert " + s + " to JSONArray.", e);
            return null;
        }
    }

    public static @NotNull Mono<Void> POST(@NotNull String uri, @Nullable JsonObject json) {
        return POST(uri, json, null, null).then();
    }

    public static <T, R> @NotNull Mono<R> POST(@NotNull String uri, @Nullable JsonObject json, @Nullable Function<String, T> stringTFunction, @Nullable Function<T, R> callback, @NotNull String @NotNull ... headers) {
        HttpRequest.Builder post = HttpRequest
                .newBuilder()
                .uri(URI.create(API_URL + uri));

        if (headers.length > 0)
            post = post.headers(headers);

        if (json == null)
            post = post.POST(HttpRequest.BodyPublishers.noBody());
        else
            post = post.header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()));

        return Mono
                .fromFuture(client.sendAsync(post.build(), HttpResponse.BodyHandlers.ofString(), Http::applyPushPromise))
                .map(response -> Tuples.of(response.statusCode(), response.body()))
                .<R>handle((tuple, sink) -> {
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
                })
                .onErrorResume(throwable -> {
                    getLogger().severe(throwable.getMessage());
                    return Mono.empty();
                });
    }

    private static Logger getLogger(){
        return Logger.getLogger(Http.class.getName());
    }

    private static void applyPushPromise(HttpRequest initiatingRequest, HttpRequest pushPromiseRequest, Function<HttpResponse.BodyHandler<String>, CompletableFuture<HttpResponse<String>>> acceptor) { }

}

