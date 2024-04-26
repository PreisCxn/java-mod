package de.alive.api.networking;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface Http {
    String getApiUrl();

    Mono<String> GET(String uri);

    @NotNull
    Mono<String> GET(String baseUri, String uri);

    @NotNull
    Mono<String> POST(@NotNull String uri, @Nullable JsonObject json);

    Mono<byte[]> getBytes(String baseUrl, String s);
}
