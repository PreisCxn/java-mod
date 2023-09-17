package de.alive.pricecxn;

import com.google.gson.JsonElement;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.function.Function;

public interface DataAccess {
    List<String> getData();

    void setDataHandler(DataHandler dataHandler);

    boolean hasProcessData();
    boolean hasEqualData();

    Function<JsonElement, JsonElement> getProcessData();
    Function<Pair<JsonElement, JsonElement>, Boolean> getEqualData();

}
