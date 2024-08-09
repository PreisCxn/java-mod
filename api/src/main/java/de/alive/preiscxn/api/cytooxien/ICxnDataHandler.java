package de.alive.preiscxn.api.cytooxien;

import de.alive.preiscxn.api.networking.DataHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Pattern;

public interface ICxnDataHandler {
    Pattern UUID_PATTERN = Pattern.compile("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$");
    @NotNull
    Mono<Void> initData();

    @NotNull
    Mono<Void> refreshItemData(String dataKey, boolean isNook);

    @NotNull
    Mono<Void> refreshData(boolean forced);

    DataHandler get(String key);

    DataHandler getData(String key);

    @Nullable
    List<ModUser> getModUsers();

}
