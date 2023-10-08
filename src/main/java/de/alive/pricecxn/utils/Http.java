package de.alive.pricecxn.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Http {

    public static final String API_URL = "";

    private static final @NotNull HttpClient client = HttpClient.newHttpClient();

    public static <T, R> CompletableFuture<R> GET(String uri, Function<String, T> stringTFunction, Function<T, R> callback, String... headers) {
        return GET(API_URL, uri, stringTFunction, callback, headers);
    }

    public static <T, R> CompletableFuture<R> GET(String baseUri, String uri, Function<String, T> stringTFunction, Function<T, R> callback, String... headers) {
        HttpRequest.Builder get = HttpRequest.newBuilder()
                .uri(URI.create(baseUri + uri))
                .GET();

        if (headers.length > 0)
            get = get.headers(headers);

        return client.sendAsync(get.build(), HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    System.out.println("testing");
                    int statusCode = response.statusCode();

                    System.out.println(response.body());

                    if (statusCode >= 200 && statusCode < 300) {
                        T apply = stringTFunction.apply(response.body());
                        if (apply != null) {
                            return callback.apply(apply);
                        }
                    } else {
                        String errorMessage = "Received wrong success code: " + statusCode;
                        System.err.println(errorMessage);
                        throw new IllegalStateException(response.body());
                    }

                    return null;
                })
                .exceptionally(ex -> {
                    System.err.println("An error occurred while making the GET request. ");
                    ex.printStackTrace();
                    return null;
                });
    }

    public static CompletableFuture<Void> PUT(@NotNull String uri, @Nullable JsonObject json, @NotNull String... headers) {
        return PUT(uri, json, null, null, headers);
    }

    public static <T, R> CompletableFuture<R> PUT(@NotNull String uri, @Nullable JsonObject json, @Nullable Function<String, T> stringTFunction, @Nullable Function<T, R> callback, @NotNull String... headers) {
        HttpRequest.Builder putBuilder = buildPutBuilder(uri, json, headers);

        return client.sendAsync(putBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    int statusCode = response.statusCode();

                    if (statusCode >= 200 && statusCode < 300) {
                        if(stringTFunction == null)
                            return null;

                        T apply = stringTFunction.apply(response.body());
                        if(apply == null)
                            return null;


                        if (callback != null) {
                            return callback.apply(apply);
                        }
                    } else {
                        String errorMessage = "PUT request to " + uri + " failed with status code: " + statusCode;
                        System.err.println(errorMessage);
                        throw new IllegalStateException(response.body());
                    }

                    return null;
                })
                .exceptionally(ex -> {
                    System.err.println("An error occurred while making the PUT request. ");
                    ex.printStackTrace();
                    return null;
                });
    }

    private static HttpRequest.Builder buildPutBuilder(@NotNull String uri, @Nullable JsonObject json, @NotNull String... headers) {
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

    public static @Nullable JsonObject JsonObjectConverter(String s) {
        try{
            return JsonParser.parseString(s).getAsJsonObject();
        }catch(JsonSyntaxException e){
            System.err.println("Could not convert " + s + " to JsonObject.");
            e.printStackTrace();
            return null;
        }
    }

    @Contract("_ -> new")
    public static @Nullable JsonArray JsonArrayConverter(String s) {
        try{
            return JsonParser.parseString(s).getAsJsonArray();
        }catch(JsonSyntaxException e){
            System.err.println("Could not convert " + s + " to JsonArray.");
            e.printStackTrace();
            return null;
        }
    }

    public static CompletableFuture<Void> POST(@NotNull String uri, @Nullable JsonObject json) {
        return POST(uri, json, null, null);
    }

    public static <T, R> CompletableFuture<R> POST(@NotNull String uri, @Nullable JsonObject json, @Nullable Function<String, T> stringTFunction, @Nullable Function<T, R> callback, @NotNull String... headers) {
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

        return client.sendAsync(post.build(), HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    int statusCode = response.statusCode();

                    if (statusCode >= 200 && statusCode < 300) {
                        if (stringTFunction == null) return null;
                        T apply = stringTFunction.apply(response.body());
                        if (apply != null && callback != null) {
                            return callback.apply(apply);
                        }
                    } else {
                        String errorMessage = "Received wrong success code: " + statusCode;
                        System.err.println(errorMessage);
                        throw new IllegalStateException(response.body());
                    }

                    return null;
                })
                .exceptionally(ex -> {
                    System.err.println("An error occurred while making the POST request. ");
                    ex.printStackTrace();
                    return null;
                });
    }

}

