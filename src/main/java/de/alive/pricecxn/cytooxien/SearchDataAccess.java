package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.DataHandler;
import de.alive.pricecxn.DataAccess;
import io.netty.util.internal.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public enum SearchDataAccess implements DataAccess {

    //Inventory Searches
    INV_AUCTION_HOUSE_SEARCH("", List.of("Auktionshaus")),
    INV_ITEM_SHOP_SEARCH("", List.of("Spieler-Shop")),
    INV_NOOK_SEARCH("", List.of("Shop")),
    INV_TRADE_SEARCH("", List.of("Handel")),

    //ItemData Searches
    TIMESTAMP_SEARCH("", List.of("Ende: ")),
    SELLER_SEARCH("", List.of("Verkäufer: ")),
    BID_SEARCH("", List.of("Gebotsbetrag: ")),
    BUY_SEARCH("", List.of("Sofortkauf: ")),
    THEME_SERVER_SEARCH("", List.of("Du befindest dich auf")),

    //Time Searches
    HOUR_SEARCH("", List.of("Stunde")),
    MINUTE_SEARCH("", List.of("Minute")),
    SECOND_SEARCH("", List.of("Sekunde")),
    NOW_SEARCH("", List.of("Jetzt"));

    private String id;
    private List<String> backupData;

    private DataHandler dataHandler = null;

    private Function<String, String> processData = null;

    SearchDataAccess(String id, List<String> backupData, @Nullable Function<String, String> processData) {
        this.id = id;
        this.backupData = backupData;
        this.processData = processData;
    }

    SearchDataAccess(String id, List<String> backupData) {
        this(id, backupData, null);
    }

    public List<String> getData(){
        if(dataHandler == null || dataHandler.getData() == null || !dataHandler.getData().containsKey(id))
            return backupData;
        else
            return dataHandler.getData().get(id);
    }

    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public Function<String, String> getProcessData() {
        return processData;
    }

    public boolean hasProcessData(){
        return processData != null;
    }
}
