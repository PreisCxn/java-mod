package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonObject;
import de.alive.pricecxn.networking.DataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;

public interface PriceCxnItemStack {

    public static final String ITEM_NAME_KEY = "itemName";
    public static final String AMOUNT_KEY = "amount";
    public static final String COMMENT_KEY = "comment";
    public static final String DISPLAY_NAME_KEY = "displayName";
    public static final String MC_CLIENT_LANG_KEY = "mcClientLang";

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
