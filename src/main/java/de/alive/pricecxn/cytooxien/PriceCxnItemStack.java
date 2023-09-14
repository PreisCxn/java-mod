package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PriceCxnItemStack {

    public static final String ITEM_NAME = "itemName";
    public static final String AMOUNT = "amount";
    public static final String COMMENT = "comment";

    private final ItemStack item;
    private final Map<String, SearchDataAccess> searchData;

    private final JsonObject data = new JsonObject();

    public PriceCxnItemStack(@NotNull ItemStack item, @Nullable Map<String, SearchDataAccess> searchData) {
        this.searchData = searchData;
        this.item = item;

        data.addProperty(ITEM_NAME, item.getItem().getTranslationKey());
        data.addProperty(AMOUNT, String.valueOf(item.getCount()));
        data.add(COMMENT, nbtToJson(this.item));

        /*
        wird immer gesucht:
        - itemName
        - amount
        - comment (nbts)
        zusätzlich suche nach den keys in searchData
         */
        if (searchData != null) {
            searchData.forEach((key, value) -> {

            });
        }


    }

    private JsonObject nbtToJson(ItemStack item) {
        JsonObject json = new JsonObject();
        NbtCompound nbt = item.getNbt();

        if (!item.hasNbt()) return json;
        if(nbt == null) return json;

        for (String key : nbt.getKeys()) {
            if (nbt.contains(key)) {
                json.addProperty(key, Objects.requireNonNull(nbt.get(key)).toString());
            }
        }

        return json;
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    public JsonObject getData() {
        return data;
    }
}
