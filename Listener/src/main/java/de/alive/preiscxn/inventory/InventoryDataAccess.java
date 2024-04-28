package de.alive.preiscxn.inventory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.alive.api.cytooxien.DataImpl;
import de.alive.api.networking.Data;
import de.alive.api.networking.DataAccess;
import de.alive.api.utils.StringUtil;
import org.jetbrains.annotations.Nullable;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.function.Function;

public enum InventoryDataAccess implements DataAccess {
    SELLER_SEARCH("cxnprice.translation.auctions_search.seller", List.of("Verkäufer: ")),
    BID_SEARCH("cxnprice.translation.auctions_search.bid", List.of("Gebotsbetrag: "), null,
            (equal) -> equal.getT1().getAsString().equals(equal.getT2().getAsString())),
    AH_BUY_SEARCH("cxnprice.translation.auctions_search.buy", List.of("Sofortkauf: ")),
    THEME_SERVER_SEARCH("cxnprice.translation.theme_search", List.of("Du befindest dich auf")),

    HIGHEST_BIDDER_SEARCH("cxnprice.translation.auctions_search.highest_bidder",
            List.of("Höchstbietender: "),
            (result) -> new JsonPrimitive(!result.isJsonNull()),
            null,
            new JsonPrimitive(false)),
    INV_AUCTION_HOUSE_SEARCH("cxnprice.translation.auctions_search.inventory", List.of("Auktionshaus")),

    //ItemData Searches ItemShop
    SHOP_BUY_SEARCH("cxnprice.translation.item_shop.buy", List.of("Kaufen: ")),
    SHOP_SELL_SEARCH("cxnprice.translation.item_shop.sell", List.of("Verkaufen: ")),

    //Inventory Searches
    INV_ITEM_SHOP_SEARCH("cxnprice.translation.item_shop.inventory", List.of("Spieler-Shop")),
    INV_NOOK_SEARCH("cxnprice.translation.nook_shop.inventory",
            List.of("\uEA01\uE065\uEA09\uEB07\uEB05\uEB04\uEB03\uEB01\uE065\uEA09\uEB07\uEB05\uEB04\uEB03\uEB01\uE065\uEA09\uEB07\uEB05\uEB04\uEB03\uEB01\uE065\uEA09\uEB07\uEB05\uEB04\uEB03\uEB01\uEB09\uEA08\uEA07--##--\uF702\uEB09\uEA08\uEA07\uEA06\uEA05\uEA04\uEA03\uEA02\uEA01\uE420\uEA09\uEB08\uEB07\uEB06\uEB05\uEB04\uEB03\uEB02\uEB01\uEA09\uEB08\uEB02\uEB01\uEB09\uEA08\uEA07\uEA06\uEA05\uEA04\uEA02--##--\uF602\uF702\uF702\uF702\uF702\uF702\uF702\uEB09\uEA08\uEA07\uEA06\uEA05\uEA04\uEA02\uE302\uEA09\uEB08\uEB07\uEB06\uEB05\uEB04\uEB02\uEA09\uEB08\uEB07\uEB06\uEB01\uF602\uF602\uF602\uF602\uF602\uF602")),
    INV_TRADE_SEARCH("cxnprice.translation.trade.inventory", List.of("Handel")),

    //ItemData Searches Trade
    TRADE_BUY_SEARCH("cxnprice.translation.trade.buy", List.of("» "), StringUtil::removeLastChar, (equal) -> true),


    ;
    private final Data data;

    InventoryDataAccess(String id, List<String> backupData, @Nullable Function<JsonElement, JsonElement> processData, @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> equalData, @Nullable JsonElement defaultResult) {
        this.data = new DataImpl(id, backupData, processData, equalData, defaultResult);
    }

    InventoryDataAccess(String id, List<String> backupData, @Nullable Function<JsonElement, JsonElement> processData, @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> equalData) {
        this.data = new DataImpl(id, backupData, processData, equalData);
    }

    InventoryDataAccess(String id, List<String> backupData) {
        this.data = new DataImpl(id, backupData);
    }

    public Data getData() {
        return data;
    }
}
