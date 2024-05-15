package de.alive.preiscxn.core.impl;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.PriceText;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static de.alive.preiscxn.api.utils.StringUtil.convertPrice;

public class PriceTextImpl implements PriceText<PriceTextImpl> {
    private static final Component COIN_TEXT = Component.text(COIN)
            .color(NamedTextColor.YELLOW);
    private static final Style PRICE_STYLE = Style.EMPTY.color(NamedTextColor.GOLD);
    private static final String MAX_SEARCHING_POINTS = "...";
    private double priceAdder = 0;
    private double priceMultiplier = 1;
    private String identifierText = "";
    @NotNull
    private String searchingPoints = "";
    private int searchingCount = 0;
    private double @Nullable [] prices = null;
    private SearchingState isSearching;

    public PriceTextImpl(boolean isSearching) {
        this.isSearching = isSearching ? SearchingState.SEARCHING : SearchingState.FINISHED;
    }

    public @NotNull PriceTextImpl withPrices(double... prices) {
        setPrices(prices);
        return this;
    }

    public @NotNull PriceTextImpl withIdentifierText(String identifierText) {
        this.identifierText = identifierText;
        return this;
    }

    public @NotNull PriceTextImpl withPriceMultiplier(double priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
        return this;
    }

    public @NotNull PriceTextImpl withPriceMultiplier(int priceMultiplier) {
        return withPriceMultiplier((double) priceMultiplier);
    }

    public @NotNull PriceTextImpl withPriceAdder(double priceAdder) {
        this.priceAdder = priceAdder;
        return this;
    }

    public Component getText(double... prices) {
        return this.withPrices(prices).getText();
    }

    private void sortPrices() {
        if (prices != null)
            Arrays.sort(prices);
    }

    public void setPrices(double[] prices) {
        this.prices = prices;
        sortPrices();
    }

    public Component getText() {
        if (isSearching != SearchingState.FINISHED) return getSearchingText();
        PriceCxn.getMod().getLogger().debug("preise!!! ");
        PriceCxn.getMod().getLogger().debug(String.valueOf(this.priceAdder));
        return getLowerPriceText()
                .flatMap(text -> getUpperPriceText()
                        .map(mutableText -> (Component)(Component.text(identifierText.isEmpty() ? "" : identifierText + " ")
                                .color(NamedTextColor.GRAY)
                                .append(text)
                                .append(Component.text(" - "))
                                .color(NamedTextColor.GRAY)
                                .append(mutableText)
                                .append(COIN_TEXT))).or(() ->
                                Optional.ofNullable(
                                        Component.text(identifierText.isEmpty() ? "" : identifierText + " ")
                                        .color(NamedTextColor.GRAY)
                                        .append(text)
                                        .append(COIN_TEXT))))
                .orElse(getSearchingText());
    }

    private Component getSearchingText() {
        if (isSearching == SearchingState.FAILED_SEARCHING)
            return Component.translatable("cxn_listener.display_prices.search_failed")
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC);
        else {
            addingSearchingPoints();
            return Component.translatable("cxn_listener.display_prices.search", Component.text(searchingPoints))
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC);
        }
    }

    public void finishSearching() {
        this.isSearching = SearchingState.FINISHED;
    }

    @Override
    public void setIsSearching(PriceText.SearchingState isSearching) {
        this.isSearching = isSearching;
    }

    private void addingSearchingPoints() {
        if (searchingCount >= 20) {
            if (Objects.equals(this.searchingPoints, MAX_SEARCHING_POINTS))
                this.searchingPoints = "";
            else
                this.searchingPoints += ".";

            searchingCount = 0;
        } else {
            searchingCount++;
        }
    }

    private @NotNull Optional<Component> getPriceText(double price) {
        if (price == 0) return Optional.empty();
        return Optional
                .of(Component.text(
                                convertPrice(price * this.priceMultiplier + this.priceAdder))
                        .style(PRICE_STYLE));
    }

    private @NotNull Optional<Component> getLowerPriceText() {
        if (prices == null || prices.length == 0) return Optional.empty();
        return getPriceText(prices[0]);
    }

    private @NotNull Optional<Component> getUpperPriceText() {
        if (prices == null || prices.length <= 1) return Optional.empty();
        return getPriceText(prices[prices.length - 1]);
    }

    public double getPriceAdder() {
        return priceAdder;
    }
}
