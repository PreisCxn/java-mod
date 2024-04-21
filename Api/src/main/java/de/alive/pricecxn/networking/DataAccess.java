package de.alive.pricecxn.networking;

import com.google.gson.JsonElement;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public interface DataAccess {
    List<String> getData();

    void setDataHandler(DataHandler dataHandler);

    JsonElement getDefaultResult();

    boolean hasProcessData();
    boolean hasEqualData();

    @Nullable Function<JsonElement, JsonElement> getProcessData();
    @Nullable Function<Pair<JsonElement, JsonElement>, Boolean> getEqualData();

}
