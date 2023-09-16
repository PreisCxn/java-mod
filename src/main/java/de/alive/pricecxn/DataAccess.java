package de.alive.pricecxn;

import java.util.List;
import java.util.function.Function;

public interface DataAccess {

    List<String> getData();

    void setDataHandler(DataHandler dataHandler);

    boolean hasProcessData();

    Function<String, String> getProcessData();

}
