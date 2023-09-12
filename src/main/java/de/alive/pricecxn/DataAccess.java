package de.alive.pricecxn;

import java.util.List;

public interface DataAccess {

    List<String> getData();

    void setDataHandler(DataHandler dataHandler);

}
