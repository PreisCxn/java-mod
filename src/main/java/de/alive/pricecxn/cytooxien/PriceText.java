package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.utils.TimeUtil;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static de.alive.pricecxn.utils.StringUtil.convertPrice;

public class PriceText {
    public static final MutableText COIN_TEXT = MutableText.of(new PlainTextContent.Literal("\uE202"))
            .setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
    public static final Style PRICE_STYLE = Style.EMPTY.withColor(Formatting.GOLD);
    public static final Style GRAY_STYLE = Style.EMPTY.withColor(Formatting.GRAY);
    public static final String MAX_SEARCHING_POINTS = "...";
    private double priceAdder = 0;
    private double priceMultiplier = 1;
    private String identifierText = "";
    private String searchingPoints = "";
    private int searchingCount = 0;
    private double[] prices = null;
    private SearchingState isSearching = SearchingState.FINISHED;

    PriceText(boolean isSearching) {
        this.isSearching = isSearching ? SearchingState.SEARCHING : SearchingState.FINISHED;
    }

    public static PriceText create() {
        return PriceText.create(false);
    }

    public static PriceText create(boolean isSearching) {
        return new PriceText(isSearching);
    }

    public PriceText withPrices(double... prices) {
        setPrices(prices);
        return this;
    }

    public PriceText withIdentifierText(String identifierText) {
        this.identifierText = identifierText;
        return this;
    }

    public PriceText withPriceMultiplier(double priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
        return this;
    }

    public PriceText withPriceMultiplier(int priceMultiplier) {
        return withPriceMultiplier((double) priceMultiplier);
    }

    public PriceText withPriceAdder(double priceAdder) {
        this.priceAdder = priceAdder;
        return this;
    }

    public MutableText getText(double... prices) {
        return this.withPrices(prices).getText();
    }

    private void sortPrices() {
        Arrays.sort(prices);
    }

    public void setPrices(double[] prices) {
        this.prices = prices;
        sortPrices();
    }

    public MutableText getText() {
        if (isSearching != SearchingState.FINISHED) return getSearchingText();
        System.out.println("preise!!! ");
        System.out.println(this.priceAdder);
        return getLowerPriceText().flatMap(text -> getUpperPriceText().map(mutableText -> MutableText.of(new PlainTextContent.Literal(identifierText.isEmpty() ? "" : identifierText + " ")).setStyle(GRAY_STYLE)
                        .append(text)
                        .append(MutableText.of(new PlainTextContent.Literal(
                                " - ")).setStyle(GRAY_STYLE))
                        .append(mutableText)
                        .append(COIN_TEXT)).or(() -> Optional.ofNullable(MutableText.of(new PlainTextContent.Literal(identifierText.isEmpty() ? "" : identifierText + " ")).setStyle(GRAY_STYLE)
                        .append(text)
                        .append(COIN_TEXT))))
                .orElse(getSearchingText());
    }

    private MutableText getSearchingText() {
        if(isSearching == SearchingState.FAILED_SEARCHING)
            return Text.translatable("cxn_listener.display_prices.search_failed")
                    .setStyle(GRAY_STYLE.withFormatting(Formatting.ITALIC));
        else {
            addingSearchingPoints();
            return Text.translatable("cxn_listener.display_prices.search", searchingPoints)
                    .setStyle(GRAY_STYLE.withFormatting(Formatting.ITALIC));
        }
    }

    public void finishSearching() {
        this.isSearching = SearchingState.FINISHED;
    }

    public void setIsSearching(SearchingState isSearching) {
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

    private Optional<MutableText> getPriceText(double price) {
        if (price == 0) return Optional.empty();
        return Optional
                .of(MutableText
                        .of(new PlainTextContent.Literal(
                                convertPrice(price * this.priceMultiplier + this.priceAdder)))
                        .setStyle(PRICE_STYLE));
    }

    private Optional<MutableText> getLowerPriceText() {
        if (prices.length == 0) return Optional.empty();
        return getPriceText(prices[0]);
    }

    private Optional<MutableText> getUpperPriceText() {
        if (prices.length <= 1) return Optional.empty();
        return getPriceText(prices[prices.length - 1]);
    }

    public static Text space() {
        return Text.of(" ");
    }

    public enum SearchingState {
        SEARCHING,
        FINISHED,
        FAILED_SEARCHING
    }

    public double getPriceAdder() {
        return priceAdder;
    }
}
