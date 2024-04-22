package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.networking.DataHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICxnDataHandler {

    @NotNull
    Mono<Void> initData();

    @NotNull
    Mono<Void> refreshItemData(String dataKey, boolean isNook);

    @NotNull
    Mono<Void> refreshData(boolean forced);

    DataHandler get(String key);

    DataHandler getData(String key);

    @Nullable
    List<String> getModUsers();

}
