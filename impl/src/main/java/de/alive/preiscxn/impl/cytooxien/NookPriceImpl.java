package de.alive.preiscxn.impl.cytooxien;

import com.google.gson.JsonObject;
import de.alive.preiscxn.api.cytooxien.NookPrice;

public class NookPriceImpl implements NookPrice {
    private final JsonObject itemInfo;

    public NookPriceImpl(JsonObject itemInfo) {
        this.itemInfo = itemInfo;
    }

    @Override
    public boolean isEmpty() {
        return itemInfo == null || itemInfo.isJsonNull();
    }

    @Override
    public double getPrice() {
        if (!itemInfo.has("price") || itemInfo.get("price").isJsonNull()) return 0;
        return itemInfo.get("price").getAsDouble();
    }
}
