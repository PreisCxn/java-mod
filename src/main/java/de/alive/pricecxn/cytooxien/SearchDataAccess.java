package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import de.alive.pricecxn.DataHandler;
import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.utils.TimeUtil;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public enum SearchDataAccess implements DataAccess {

    //Inventory Searches
    INV_AUCTION_HOUSE_SEARCH("", List.of("Auktionshaus")),
    INV_ITEM_SHOP_SEARCH("", List.of("Spieler-Shop")),
    INV_NOOK_SEARCH("", List.of("Shop")),
    INV_TRADE_SEARCH("", List.of("Handel")),

    //ItemData Searches
    TIMESTAMP_SEARCH("", List.of("Ende: "), (result) -> {
        Optional<Long> timeStamp = TimeUtil.getStartTimeStamp(result.getAsString());

        if (timeStamp.isEmpty()) return JsonNull.INSTANCE;
        else return new JsonPrimitive(timeStamp.get());
    }, (equal) -> {
        if (equal.getLeft() == JsonNull.INSTANCE && equal.getRight() == JsonNull.INSTANCE) return true;
        if (!equal.getLeft().isJsonPrimitive() || !equal.getRight().isJsonPrimitive()) return false;
        if (!equal.getLeft().getAsJsonPrimitive().isNumber() || !equal.getRight().getAsJsonPrimitive().isNumber()) return false;
        return TimeUtil.timestampsEqual(equal.getLeft().getAsLong(), equal.getRight().getAsLong(), 3);
    }),
    SELLER_SEARCH("", List.of("Verkäufer: ")),
    BID_SEARCH("", List.of("Gebotsbetrag: "), null,
            (equal) -> equal.getLeft().getAsString().equals(equal.getRight().getAsString())),
    BUY_SEARCH("", List.of("Sofortkauf: ")),
    THEME_SERVER_SEARCH("", List.of("Du befindest dich auf")),

    //Time Searches
    HOUR_SEARCH("", List.of("Stunde")),
    MINUTE_SEARCH("", List.of("Minute")),
    SECOND_SEARCH("", List.of("Sekunde")),
    NOW_SEARCH("", List.of("Jetzt"));

    private final String id;
    private final List<String> backupData;

    private DataHandler dataHandler = null;

    private final Function<JsonElement, JsonElement> processData;
    private final Function<Pair<JsonElement, JsonElement>, Boolean> equalData;

    SearchDataAccess(String id, List<String> backupData, @Nullable Function<JsonElement, JsonElement> processData, @Nullable Function<Pair<JsonElement, JsonElement>, Boolean> equalData) {
        this.id = id;
        this.backupData = backupData;
        this.processData = processData;
        this.equalData = equalData;
    }

    SearchDataAccess(String id, List<String> backupData) {
        this(id, backupData, null, null);
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

}
