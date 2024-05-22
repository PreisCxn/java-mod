package de.alive.preiscxn.api.interfaces;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.networking.DataAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public interface IItemStack {
    Pattern JSON_KEY_PATTERN = Pattern.compile("([{,])(\\w+):");
    Pattern TO_DELETE_PATTERN = Pattern.compile("[\\\\']");

    PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips);
    PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment);
    PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData);

    List<String> priceCxn$getLore();
    String priceCxn$getItemName();
    String priceCxn$getDisplayName();

    int priceCxn$getCount();

    boolean priceCxn$isTrimTemplate();
    boolean priceCxn$isNetheriteUpgradeSmithingTemplate();

    Optional<String> priceCxn$getRegistryKey();

    JsonObject priceCxn$getComponentsAsJson();

    default Object object(String nbtString) {
        if (nbtString == null)
            return "";

        nbtString = TO_DELETE_PATTERN.matcher(nbtString).replaceAll("");

        JsonObject valueJson;

        //test if only Delete Pattern is needed
        try {
            valueJson = JsonParser.parseString(nbtString).getAsJsonObject();
        } catch (IllegalStateException e) {
            nbtString = JSON_KEY_PATTERN.matcher(nbtString).replaceAll("$1\"$2\":");

            //test if JsonArray
            try {
                return JsonParser.parseString(nbtString).getAsJsonArray();
            } catch (IllegalStateException ignored) {
                //test if JsonKey is missing
                try {
                    return JsonParser.parseString(nbtString).getAsJsonObject();
                } catch (IllegalStateException e2) {
                    //else add as normal String
                    return nbtString;
                }
            }
        } catch (JsonParseException e) {
            //else add as normal String
            return nbtString;
        }

        if (valueJson != null) {
            return valueJson;
        }

        return nbtString;
    }
}
