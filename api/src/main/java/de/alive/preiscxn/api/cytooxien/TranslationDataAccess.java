package de.alive.preiscxn.api.cytooxien;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import de.alive.preiscxn.api.networking.Data;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.api.utils.TimeUtil;
import org.jetbrains.annotations.Nullable;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public enum TranslationDataAccess implements DataAccess {

    //ItemData Searches AuctionHouse
    TIMESTAMP_SEARCH("cxnprice.translation.auction_searches.timestamp", List.of("Ende: "), result -> {
        Optional<Long> timeStamp = TimeUtil.getStartTimeStamp(result.getAsString());

        if (timeStamp.isEmpty()) return JsonNull.INSTANCE;
        else return new JsonPrimitive(timeStamp.get());
    }, equal -> {
        if (equal.getT1() == JsonNull.INSTANCE && equal.getT2() == JsonNull.INSTANCE) return true;
        if (!equal.getT1().isJsonPrimitive() || !equal.getT2().isJsonPrimitive()) return false;
        if (!equal.getT1().getAsJsonPrimitive().isNumber() || !equal.getT2().getAsJsonPrimitive().isNumber())
            return false;
        return TimeUtil.timestampsEqual(equal.getT1().getAsLong(), equal.getT2().getAsLong(), 3);
    }),
    THEME_SERVER_SEARCH("cxnprice.translation.theme_search", List.of("Du befindest dich auf")),

    //Time Searches
    HOUR_SEARCH("cxnprice.translation.time_search.hour", List.of("Stunde")),
    MINUTE_SEARCH("cxnprice.translation.time_search.minute", List.of("Minute")),
    SECOND_SEARCH("cxnprice.translation.time_search.second", List.of("Sekunde")),
    NOW_SEARCH("cxnprice.translation.time_search.now", List.of("Jetzt")),

    //Inv blocks
    SKYBLOCK_INV_BLOCK("cxnprice.translation.inv_block.skyblock", List.of("Inseln")),
    CITYBUILD_INV_BLOCK("cxnprice.translation.inv_block.citybuild", List.of("Stadt")),

    //Transaction
    TRANSACTION_COUNT("cxnprice.translation.transaction.count", List.of("Anzahl:")),
    TRANSACTION_TITLE("cxnprice.translation.transaction.title", List.of("Deine Transaktionen")),

    VISIT_ISLAND("cxnprice.translation.visit.island", List.of("» Klicke um die Insel zu besuchen!"));

    private final Data data;

    TranslationDataAccess(String id,
                          List<String> backupData,
                          @Nullable Function<JsonElement, JsonElement> processData,
                          @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> equalData,
                          @Nullable JsonElement defaultResult) {
        this.data = new Data(id, backupData, processData, equalData, defaultResult);
    }

    TranslationDataAccess(String id,
                          List<String> backupData,
                          @Nullable Function<JsonElement, JsonElement> processData,
                          @Nullable Function<Tuple2<JsonElement, JsonElement>, Boolean> equalData) {
        this.data = new Data(id, backupData, processData, equalData);
    }

    TranslationDataAccess(String id, List<String> backupData) {
        this.data = new Data(id, backupData);
    }

    public Data getData() {
        return data;
    }
}
