package de.alive.pricecxn.networking.cdn;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import de.alive.pricecxn.networking.Http;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class CdnDeliveryType<T> {

    public static final CdnDeliveryType<String> FILE = new CdnDeliveryType<>(null, Function.identity());
    public static final CdnDeliveryType<JsonArray> LIST_FILES = new CdnDeliveryType<>("type=list-files", s -> JsonParser.parseString(s).getAsJsonArray());
    public static final CdnDeliveryType<JsonArray> LIST_VERSIONS = new CdnDeliveryType<>("type=list-versions", s -> JsonParser.parseString(s).getAsJsonArray());
    public static final CdnDeliveryType<String> NEWEST_VERSION = new CdnDeliveryType<>("type=newest-version", Function.identity());
    public static final CdnDeliveryType<String> HASH = new CdnDeliveryType<>("type=hash", Function.identity());

    private static final String BASE_URL = "https://cdn.preiscxn.de/";

    private final String type;
    private final Function<String, T> stringTFunction;
    private final Http http;

    private CdnDeliveryType(String type, Function<String, T> stringTFunction) {
        this.http = Http.getInstance();
        this.type = type;
        this.stringTFunction = stringTFunction;
    }

    public @NotNull Mono<T> generateResponse(String filePath) {
        return http.GET(BASE_URL, filePath + (this.type == null ? "" : "?" + this.type))
                .map(stringTFunction);
    }

    public <P> @NotNull Mono<P> generateResponse(String filePath, @NotNull Function<T, P> function) {
        return http.GET(BASE_URL, filePath + "?" + this.type)
                .map(stringTFunction)
                .map(function);
    }

    public Mono<byte[]> generateResponseAsBytes(String remotePath) {
        return http.getBytes(BASE_URL, remotePath + (this.type == null ? "" : "?" + this.type));
    }

}