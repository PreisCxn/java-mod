package de.alive.preiscxn.impl.networking.cdn;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.alive.api.PriceCxn;
import de.alive.api.networking.Http;
import de.alive.api.networking.cdn.CdnFileHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static de.alive.api.LogPrinter.LOGGER;

public class CdnFileHandlerImpl implements CdnFileHandler {

    private static final String BASE_URL = "https://cdn.preiscxn.de/";

    private final Http http;

    public CdnFileHandlerImpl(Http http) {
        this.http = http;
    }

    private String getPath(@NotNull String file, @Nullable String version) {
        boolean questionMark = false;
        if (PriceCxn.RELEASE_CHANNEL != null) {
            file += "?channel=" + PriceCxn.RELEASE_CHANNEL;
            questionMark = true;
        }

        if (version != null) {
            file += (questionMark ? "&" : "?") + "version=" + version;
        }

        return file;
    }

    @Override
    public @NotNull Mono<byte[]> getFile(@NotNull String file, @Nullable String version) {
        return http.getBytes(BASE_URL, getPath(file, version));
    }

    @Override
    public @NotNull Mono<List<String>> getFiles(@NotNull String prefix) {
        return http.get(BASE_URL, prefix)
                .map(s -> {
                    JsonArray jsonElements = JsonParser.parseString(s).getAsJsonArray();
                    List<String> files = new ArrayList<>();
                    for (JsonElement jsonElement : jsonElements) {
                        files.add(jsonElement.getAsString());
                    }
                    return files;
                });
    }

    @Override
    public @NotNull Mono<List<String>> getVersions(@NotNull String file) {
        return http.get(BASE_URL, file + "?type=list-versions")
                .map(s -> {
                    JsonArray jsonElements = JsonParser.parseString(s).getAsJsonArray();
                    List<String> versions = new ArrayList<>();
                    for (JsonElement jsonElement : jsonElements) {
                        versions.add(jsonElement.getAsString());
                    }
                    return versions;
                });
    }

    @Override
    public @NotNull Mono<String> getNewestVersion(@NotNull String file) {
        return http.get(BASE_URL, file + "?type=newest-version")
                .map(s -> {
                    JsonElement jsonElement = JsonParser.parseString(s);
                    if (jsonElement.isJsonNull())
                        return "";
                    return jsonElement.getAsString();
                });
    }

    @Override
    public @NotNull Mono<String> getHash(@NotNull String file, @Nullable String version) {
        String path = getPath(file, version);

        if (path.contains("?"))
            path += "&type=hash";
        else
            path += "?type=hash";

        String finalPath = path;
        return http.get(BASE_URL, path)
                .mapNotNull(s -> {
                    JsonElement jsonElement = JsonParser.parseString(s);
                    if (jsonElement.isJsonNull()) {
                        LOGGER.error("Hash is null from url: {} and return: '{}'", BASE_URL + finalPath, s);
                        return null;
                    }
                    return jsonElement.getAsString();
                });
    }

}
