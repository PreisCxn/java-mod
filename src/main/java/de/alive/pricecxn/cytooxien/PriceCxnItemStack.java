package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.utils.StringUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class PriceCxnItemStack {

    private static final Pattern JSON_KEY_PATTERN = Pattern.compile("([{,])(\\w+):");
    private static final Pattern TO_DELETE_PATTERN = Pattern.compile("[\\\\']");

    public static final String ITEM_NAME_KEY = "itemName";
    public static final String AMOUNT_KEY = "amount";
    public static final String COMMENT_KEY = "comment";

    private final ItemStack item;
    private final Map<String, DataAccess> searchData;

    private final JsonObject data = new JsonObject();

    private final List<String> toolTips;

    public PriceCxnItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData) {

        this.searchData = searchData;
        this.item = item;
        this.toolTips = StringUtil.getToolTips(this.item);

        /*
        wird immer gesucht:
        - itemName
        - amount
        - comment (nbts)
         */
        data.addProperty(ITEM_NAME_KEY, item.getItem().getTranslationKey());
        data.addProperty(AMOUNT_KEY, item.getCount());
        data.add(COMMENT_KEY, nbtToJson(this.item));

        /*
        zusätzlich suche nach den keys in searchData:
         */

        if (this.searchData != null) {
            for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {

                DataAccess access = entry.getValue();

                String searchResult = access.hasProcessData() ? access.getProcessData().apply(this.toolTipSearch(access)) : this.toolTipSearch(access);

                if(searchResult == null){
                    data.add(entry.getKey(), JsonNull.INSTANCE);
                } else
                    data.addProperty(entry.getKey(), searchResult);
            }
        }

    }

    private JsonObject nbtToJson(ItemStack item) {
        JsonObject json = new JsonObject();
        NbtCompound nbt = item.getNbt();

        if (!item.hasNbt()) return json;
        if (nbt == null) return json;

        for (String key : nbt.getKeys()) {
            if (nbt.contains(key)) {
                String nbtString = Optional.ofNullable(nbt.get(key)).map(NbtElement::asString).orElse(null);

                if (nbtString == null) continue;

                nbtString = TO_DELETE_PATTERN.matcher(nbtString).replaceAll("");

                JsonObject valueJson = null;

                try {
                    valueJson = JsonParser.parseString(nbtString).getAsJsonObject();
                } catch (IllegalStateException e) {

                    nbtString = JSON_KEY_PATTERN.matcher(nbtString).replaceAll("$1\"$2\":");

                    try {
                        valueJson = JsonParser.parseString(nbtString).getAsJsonObject();
                    } catch (IllegalStateException e2) {
                        json.addProperty(key, Objects.requireNonNull(nbt.get(key)).asString());
                    }

                }

                if (valueJson != null) json.add(key, valueJson);
            }
        }

        return json;
    }

    private @Nullable String toolTipSearch(@NotNull DataAccess access) {
        System.out.println(this.toolTips);
        String result;
        for (String prefix : access.getData()) {
            result = StringUtil.getFirstSuffixStartingWith(this.toolTips, prefix);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public int hashCode() {
        return this.data.hashCode();
    }

    @Override
    public String toString() {
        return this.data.toString();
    }

    public JsonObject getData() {
        return data;
    }
}
