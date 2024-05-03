package de.alive.api.cytooxien;

import com.google.gson.JsonObject;
import de.alive.api.networking.DataAccess;
import de.alive.api.networking.IServerChecker;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

    /**
     * This includes the stack amount, but also spawn amount.
     */
    int getAdvancedAmount(IServerChecker serverChecker, AtomicReference<PriceText> pcxnPriceText, List<Text> list);

    @NotNull
    Map<String, DataAccess> getSearchData();

    @Nullable
    JsonObject findItemInfo(String dataKey);

    enum ViewMode {
        SINGLE("cxn_listener.display_prices.amount_type.single"),
        CURRENT_STACK("cxn_listener.display_prices.current_stack"),;

        private final String translationString;

        ViewMode(String translationString) {
            this.translationString = translationString;
        }

        public String getTranslationString() {
            return translationString;
        }
    }
}
