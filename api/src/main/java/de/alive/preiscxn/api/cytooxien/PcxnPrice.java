package de.alive.preiscxn.api.cytooxien;

public interface PcxnPrice {
    boolean isEmpty();

    boolean has(String key);

    double getLowerPrice();

    double getUpperPrice();

    String getTimestamp();

    String getPbvSearchKey();

    String getItemSearchKey();
}
