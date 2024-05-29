package de.alive.preiscxn.impl.cytooxien;

import com.google.gson.JsonObject;
import de.alive.preiscxn.api.cytooxien.NookPrice;

import java.util.Objects;

public class NookPriceImpl implements NookPrice {
    private final JsonObject itemInfo;

    public NookPriceImpl(JsonObject itemInfo) {
        this.itemInfo = Objects.requireNonNullElse(!itemInfo.isJsonObject() ? null : itemInfo, new JsonObject());
    }

    @Override
    public boolean isEmpty() {
        return itemInfo == null || itemInfo.isJsonNull();
    }

    @Override
    public double getPrice() {
        if (itemInfo == null || itemInfo.isJsonNull()) return 0;
        if (!itemInfo.has("price") || itemInfo.get("price").isJsonNull()) return 0;
        return itemInfo.get("price").getAsDouble();
    }
}
