package de.alive.pricecxn;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Function;

public interface DataAccess {

    List<String> getData();

    void setDataHandler(DataHandler dataHandler);

    boolean hasProcessData();
    boolean hasEqualData();

    Function<String, String> getProcessData();
    Function<JsonElement, Boolean> getEqualData();

}
