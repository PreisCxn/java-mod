package de.alive.api.networking;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.function.Function;

public class Data {
    private final String id;
    private final List<String> backupData;

    private @Nullable DataHandler dataHandler = null;

    private final @Nullable Function<JsonElement, JsonElement> processData;
    private final @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> equalData;

    private final @NotNull JsonElement defaultResult;

    public Data(String id,
                List<String> backupData,
                @Nullable Function<JsonElement, JsonElement> processData,
                @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> equalData,
                @Nullable JsonElement defaultResult) {
        this.id = id;
        this.backupData = backupData;
        this.processData = processData;
        this.equalData = equalData;
        this.defaultResult = defaultResult != null ? defaultResult : JsonNull.INSTANCE;
    }

    public Data(String id,
                List<String> backupData,
                @Nullable Function<JsonElement, JsonElement> processData,
                @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> equalData) {
        this(id, backupData, processData, equalData, null);
    }

    public Data(String id, List<String> backupData) {
        this(id, backupData, null, null, null);
    }

    public List<String> getData() {
        if (dataHandler == null || dataHandler.getData() == null || !dataHandler.getData().containsKey(id))
            return backupData;
        else return dataHandler.getData().get(id);
    }

    public void setDataHandler(@Nullable DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public @Nullable Function<JsonElement, JsonElement> getProcessData() {
        return processData;
    }

    public @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> getEqualData() {
        return equalData;
    }

    public boolean hasProcessData() {
        return processData != null;
    }

    public boolean hasEqualData() {
        return equalData != null;
    }

    public @NotNull JsonElement getDefaultResult() {
        return defaultResult;
    }
}
