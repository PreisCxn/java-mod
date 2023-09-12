package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.DataHandler;
import de.alive.pricecxn.DataAccess;

import java.util.List;

public enum SearchDataAccess implements DataAccess {
    TIMESTAMP_SEARCH("", List.of("Ende: ")),
    SELLER_SEARCH("", List.of("Verkäufer: ")),
    BID_SEARCH("", List.of("Gebotsbetrag: ")),
    BUY_SEARCH("", List.of("Sofortkauf: ")),
    THEME_SERVER_SEARCH("", List.of("Du befindest dich auf"));

    private String id;
    private List<String> backupData;

    private DataHandler dataHandler;

    SearchDataAccess(String id, List<String> backupData) {
        this.id = id;
        this.backupData = backupData;
    }

    public List<String> getData(){
        if(dataHandler == null || !dataHandler.getData().containsKey(id))
            return backupData;
        else
            return dataHandler.getData().get(id);
    }

    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

}
