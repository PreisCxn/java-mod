package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonObject;
import de.alive.pricecxn.networking.DataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface PriceCxnItemStack {

    String ITEM_NAME_KEY = "itemName";
    String AMOUNT_KEY = "amount";
    String COMMENT_KEY = "comment";
    String DISPLAY_NAME_KEY = "displayName";
    String MC_CLIENT_LANG_KEY = "mcClientLang";

    boolean deepEquals(Object o);

    @NotNull
    JsonObject getData();

    @NotNull JsonObject getDataWithoutDisplay();

    boolean isSameItem(@NotNull PriceCxnItemStack item);

    String getItemName();

    void updateData(@NotNull PriceCxnItemStack item);

    int getAmount();

    @NotNull
    Map<String, DataAccess> getSearchData();

    @Nullable
    JsonObject findItemInfo(String dataKey);

}
