package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.networking.DataHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICxnDataHandler {

    @NotNull
    Mono<Void> initData();

    @NotNull
    Mono<Void> refreshItemData(String dataKey, boolean isNook);

    @NotNull
    Mono<Void> refreshData(boolean forced);

    DataHandler get(String key);

    default void createTranslationHandler(@NotNull List<String> langList) {
        DataAccess[] translationAccess = {
                TranslationDataAccess.INV_AUCTION_HOUSE_SEARCH,
                TranslationDataAccess.INV_ITEM_SHOP_SEARCH,
                TranslationDataAccess.INV_NOOK_SEARCH,
                TranslationDataAccess.INV_TRADE_SEARCH,
                TranslationDataAccess.TIMESTAMP_SEARCH,
                TranslationDataAccess.SELLER_SEARCH,
                TranslationDataAccess.BID_SEARCH,
                TranslationDataAccess.AH_BUY_SEARCH,
                TranslationDataAccess.THEME_SERVER_SEARCH,
                TranslationDataAccess.HIGHEST_BIDDER_SEARCH,
                TranslationDataAccess.NOOK_BUY_SEARCH,
                TranslationDataAccess.SHOP_BUY_SEARCH,
                TranslationDataAccess.SHOP_SELL_SEARCH,
                TranslationDataAccess.TRADE_BUY_SEARCH,
                TranslationDataAccess.HOUR_SEARCH,
                TranslationDataAccess.MINUTE_SEARCH,
                TranslationDataAccess.SECOND_SEARCH,
                TranslationDataAccess.NOW_SEARCH,
                TranslationDataAccess.SKYBLOCK_INV_BLOCK,
                TranslationDataAccess.CITYBUILD_INV_BLOCK
        };


        data.put("cxnprice.translation",
                 new DataHandler(serverChecker,
                                 "/settings/translations",
                                 langList,
                                 "translation_key",
                                 DataHandler.TRANSLATION_REFRESH_INTERVAL, translationAccess));
    }

    DataHandler getData(String key);

    @Nullable
    List<String> getModUsers();

}
