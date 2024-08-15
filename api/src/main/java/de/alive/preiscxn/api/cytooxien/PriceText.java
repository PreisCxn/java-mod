package de.alive.preiscxn.api.cytooxien;

import org.jetbrains.annotations.NotNull;

public interface PriceText <T extends PriceText<T>> {
    String COIN = "\uE202";

    @NotNull T withPrices(double... prices);

    @NotNull T withIdentifierText(String identifierText);

    @NotNull T withPriceMultiplier(double priceMultiplier);

    default @NotNull T withPriceMultiplier(int priceMultiplier){
        return withPriceMultiplier((double) priceMultiplier);
    }

    @NotNull T withPriceAdder(double priceAdder);

    Object getText(double... prices) ;

    void setPrices(double[] prices);

    Object getText();

    void finishSearching() ;

    void setIsSearching(SearchingState isSearching);


    double getPriceAdder();

    enum SearchingState {
        SEARCHING,
        FINISHED,
        FAILED_SEARCHING
    }

}
