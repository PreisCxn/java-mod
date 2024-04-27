package de.alive.api.networking;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.function.Function;

public interface Data {
    List<String> getData();

    void setDataHandler(@Nullable DataHandler dataHandler);

    @Nullable
    Function<JsonElement, JsonElement> getProcessData();

    @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> getEqualData();

    boolean hasProcessData();

    boolean hasEqualData();

    @NotNull
    JsonElement getDefaultResult();
}
