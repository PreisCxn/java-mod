package de.alive.api.networking.cdn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CdnFileHandler {
    @NotNull Mono<byte[]> getFile(@NotNull String file, @Nullable String version);
    @NotNull Mono<List<String>> getFiles(@NotNull String prefix);
    @NotNull Mono<List<String>> getVersions(@NotNull String file);
    @NotNull Mono<String> getNewestVersion(@NotNull String file);
    @NotNull Mono<String> getHash(@NotNull String file, @Nullable String version);
}
