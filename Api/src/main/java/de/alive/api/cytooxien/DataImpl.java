package de.alive.api.cytooxien;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import de.alive.api.networking.Data;
import de.alive.api.networking.DataHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.function.Function;

public class DataImpl implements Data {
    private final String id;
    private final List<String> backupData;

    private @Nullable DataHandler dataHandler = null;

    private final @Nullable Function<JsonElement, JsonElement> processData;
    private final @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> equalData;

    private final @NotNull JsonElement defaultResult;

    public DataImpl(String id, List<String> backupData, @Nullable Function<JsonElement, JsonElement> processData, @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> equalData, @Nullable JsonElement defaultResult) {
        this.id = id;
        this.backupData = backupData;
        this.processData = processData;
        this.equalData = equalData;
        this.defaultResult = defaultResult != null ? defaultResult : JsonNull.INSTANCE;
    }

    public DataImpl(String id, List<String> backupData, @Nullable Function<JsonElement, JsonElement> processData, @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> equalData) {
        this(id, backupData, processData, equalData, null);
    }

    public DataImpl(String id, List<String> backupData) {
        this(id, backupData, null, null, null);
    }

    @Override
    public List<String> getData() {
        if (dataHandler == null || dataHandler.getData() == null || !dataHandler.getData().containsKey(id))
            return backupData;
        else return dataHandler.getData().get(id);
    }

    @Override
    public void setDataHandler(@Nullable DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    @Override
    public @Nullable Function<JsonElement, JsonElement> getProcessData() {
        return processData;
    }

    @Override
    public @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> getEqualData() {
        return equalData;
    }

    @Override
    public boolean hasProcessData() {
        return processData != null;
    }

    @Override
    public boolean hasEqualData() {
        return equalData != null;
    }

    @Override
    public @NotNull JsonElement getDefaultResult() {
        return defaultResult;
    }
}
