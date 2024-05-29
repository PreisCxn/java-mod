package de.alive.preiscxn.impl.cytooxien;

import com.google.gson.JsonObject;
import de.alive.preiscxn.api.cytooxien.PcxnPrice;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PcxnPriceImpl implements PcxnPrice {
    private final @NotNull JsonObject jsonObject;

    public PcxnPriceImpl(JsonObject jsonObject) {
        this.jsonObject = Objects.requireNonNullElse(!jsonObject.isJsonObject() ? null : jsonObject, new JsonObject());
    }

    @Override
    public boolean isEmpty() {
        return jsonObject.isEmpty();
    }

    @Override
    public boolean has(String key) {
        if(key == null || key.isEmpty()) return false;
        return jsonObject.has(key) && !jsonObject.get(key).isJsonNull();
    }

    @Override
    public double getLowerPrice() {
        if (!jsonObject.has("lower_price") || jsonObject.get("lower_price").isJsonNull()) return 0;
        return roundIfNecessary(jsonObject.get("lower_price").getAsDouble());
    }

    @Override
    public double getUpperPrice() {
        if (!jsonObject.has("upper_price") || jsonObject.get("upper_price").isJsonNull()) return 0;
        return roundIfNecessary(jsonObject.get("upper_price").getAsDouble());
    }

    @Override
    public String getTimestamp() {
        if (!jsonObject.has("timestamp") || jsonObject.get("timestamp").isJsonNull()) return null;
        return jsonObject.get("timestamp").getAsString();
    }

    @Override
    public String getPbvSearchKey() {
        if (!jsonObject.has("pbv_search_key") || jsonObject.get("pbv_search_key").isJsonNull()) return null;
        return jsonObject.get("pbv_search_key").getAsString();
    }

    @Override
    public String getItemSearchKey() {
        if (!jsonObject.has("item_search_key") || jsonObject.get("item_search_key").isJsonNull()) return null;
        return jsonObject.get("item_search_key").getAsString();
    }

    private double roundIfNecessary(double value) {
        if (value > 100) return Math.round(value);
        return value;
    }
}
