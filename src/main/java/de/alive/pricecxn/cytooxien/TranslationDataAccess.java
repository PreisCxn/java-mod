package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import de.alive.pricecxn.networking.DataHandler;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.utils.StringUtil;
import de.alive.pricecxn.utils.TimeUtil;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public enum TranslationDataAccess implements DataAccess {

    //Inventory Searches
    INV_AUCTION_HOUSE_SEARCH("cxnprice.translation.auctions_search.inventory", List.of("Auktionshaus")),
    INV_ITEM_SHOP_SEARCH("cxnprice.translation.item_shop.inventory", List.of("Spieler-Shop")),
    INV_NOOK_SEARCH("cxnprice.translation.nook_shop.inventory", List.of("Tom Block")),
    INV_TRADE_SEARCH("cxnprice.translation.trade.inventory", List.of("Handel")),

    //ItemData Searches AuctionHouse
    TIMESTAMP_SEARCH("cxnprice.translation.auction_searches.timestamp", List.of("Ende: "), (result) -> {
        Optional<Long> timeStamp = TimeUtil.getStartTimeStamp(result.getAsString());

        if (timeStamp.isEmpty()) return JsonNull.INSTANCE;
        else return new JsonPrimitive(timeStamp.get());
    }, (equal) -> {
        if (equal.getLeft() == JsonNull.INSTANCE && equal.getRight() == JsonNull.INSTANCE) return true;
        if (!equal.getLeft().isJsonPrimitive() || !equal.getRight().isJsonPrimitive()) return false;
        if (!equal.getLeft().getAsJsonPrimitive().isNumber() || !equal.getRight().getAsJsonPrimitive().isNumber()) return false;
        return TimeUtil.timestampsEqual(equal.getLeft().getAsLong(), equal.getRight().getAsLong(), 3);
    }),
    SELLER_SEARCH("cxnprice.translation.auctions_search.seller", List.of("Verkäufer: ")),
    BID_SEARCH("cxnprice.translation.auctions_search.bid", List.of("Gebotsbetrag: "), null,
            (equal) -> equal.getLeft().getAsString().equals(equal.getRight().getAsString())),
    AH_BUY_SEARCH("cxnprice.translation.auctions_search.buy", List.of("Sofortkauf: ")),
    THEME_SERVER_SEARCH("cxnprice.translation.theme_search", List.of("Du befindest dich auf")),

    HIGHEST_BIDDER_SEARCH("cxnprice.translation.auctions_search.highest_bidder", List.of("Höchstbietender: "), (result) -> new JsonPrimitive(!result.isJsonNull()), null, new JsonPrimitive(false)),

    //ItemData Searches NookShop
    NOOK_BUY_SEARCH("cxnprice.translation.nook_shop.buy", List.of("\uE204\uE211\uE212\uE212\uE212\uE212\uE212P\uE210R\uE210E\uE210I\uE210S ")),

    //ItemData Searches ItemShop
    SHOP_BUY_SEARCH("cxnprice.translation.item_shop.buy", List.of("Kaufen: ")),
    SHOP_SELL_SEARCH("cxnprice.translation.item_shop.sell", List.of("Verkaufen: ")),

    //ItemData Searches Trade
    TRADE_BUY_SEARCH("cxnprice.translation.trade.buy", List.of("» "), StringUtil::removeLastChar, (equal) -> true),

    //Time Searches
    HOUR_SEARCH("cxnprice.translation.time_search.hour", List.of("Stunde")),
    MINUTE_SEARCH("cxnprice.translation.time_search.minute", List.of("Minute")),
    SECOND_SEARCH("cxnprice.translation.time_search.second", List.of("Sekunde")),
    NOW_SEARCH("cxnprice.translation.time_search.now", List.of("Jetzt"));

    private final String id;
    private final List<String> backupData;

    private DataHandler dataHandler = null;

    private final Function<JsonElement, JsonElement> processData;
    private final Function<Pair<JsonElement, JsonElement>, Boolean> equalData;

    private final JsonElement defaultResult;

    TranslationDataAccess(String id, List<String> backupData, @Nullable Function<JsonElement, JsonElement> processData, @Nullable Function<Pair<JsonElement, JsonElement>, Boolean> equalData, @Nullable JsonElement defaultResult) {
        this.id = id;
        this.backupData = backupData;
        this.processData = processData;
        this.equalData = equalData;
        this.defaultResult = defaultResult != null ? defaultResult : JsonNull.INSTANCE;
    }

    TranslationDataAccess(String id, List<String> backupData, @Nullable Function<JsonElement, JsonElement> processData, @Nullable Function<Pair<JsonElement, JsonElement>, Boolean> equalData) {
        this(id, backupData, processData, equalData, null);
    }

    TranslationDataAccess(String id, List<String> backupData) {
        this(id, backupData, null, null, null);
    }

    public List<String> getData() {
        if (dataHandler == null || dataHandler.getData() == null || !dataHandler.getData().containsKey(id))
            return backupData;
        else return dataHandler.getData().get(id);
    }

    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public Function<JsonElement, JsonElement> getProcessData() {
        return processData;
    }

    public Function<Pair<JsonElement, JsonElement>, Boolean> getEqualData() {
        return equalData;
    }

    public boolean hasProcessData() {
        return processData != null;
    }

    public boolean hasEqualData() {
        return equalData != null;
    }

    public JsonElement getDefaultResult() {
        return defaultResult;
    }

}
