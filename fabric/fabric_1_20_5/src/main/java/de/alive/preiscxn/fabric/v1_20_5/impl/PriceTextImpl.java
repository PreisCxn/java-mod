package de.alive.preiscxn.fabric.v1_20_5.impl;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.PriceText;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static de.alive.preiscxn.api.utils.StringUtil.convertPrice;

public class PriceTextImpl implements PriceText<PriceTextImpl> {
    private static final MutableText COIN_TEXT = MutableText.of(new PlainTextContent.Literal(COIN))
            .setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
    private static final Style PRICE_STYLE = Style.EMPTY.withColor(Formatting.GOLD);
    private static final Style GRAY_STYLE = Style.EMPTY.withColor(Formatting.GRAY);
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

    public @NotNull PriceTextImpl withPriceAdder(double priceAdder) {
        this.priceAdder = priceAdder;
        return this;
    }

    public MutableText getText(double... prices) {
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

    public MutableText getText() {
        if (isSearching != SearchingState.FINISHED) return getSearchingText();
        PriceCxn.getMod().getLogger().debug("preise!!! ");
        PriceCxn.getMod().getLogger().debug(String.valueOf(this.priceAdder));
        return getLowerPriceText()
                .flatMap(text -> getUpperPriceText()
                        .map(mutableText -> MutableText.of(new PlainTextContent.Literal(identifierText.isEmpty() ? "" : identifierText + " "))
                                .setStyle(GRAY_STYLE)
                                .append(text)
                                .append(MutableText.of(new PlainTextContent.Literal(" - "))
                                        .setStyle(GRAY_STYLE))
                                .append(mutableText)
                                .append(COIN_TEXT)).or(() ->
                                Optional.ofNullable(MutableText.of(
                                                new PlainTextContent.Literal(identifierText.isEmpty() ? "" : identifierText + " "))
                                        .setStyle(GRAY_STYLE)
                                        .append(text)
                                        .append(COIN_TEXT))))
                .orElse(getSearchingText());
    }

    private MutableText getSearchingText() {
        if (isSearching == SearchingState.FAILED_SEARCHING)
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

    @Override
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

    private @NotNull Optional<MutableText> getPriceText(double price) {
        if (price == 0) return Optional.empty();
        return Optional
                .of(MutableText
                        .of(new PlainTextContent.Literal(
                                convertPrice(price * this.priceMultiplier + this.priceAdder)))
                        .setStyle(PRICE_STYLE));
    }

    private @NotNull Optional<MutableText> getLowerPriceText() {
        if (prices == null || prices.length == 0) return Optional.empty();
        return getPriceText(prices[0]);
    }

    private @NotNull Optional<MutableText> getUpperPriceText() {
        if (prices == null || prices.length <= 1) return Optional.empty();
        return getPriceText(prices[prices.length - 1]);
    }

    public double getPriceAdder() {
        return priceAdder;
    }
}
