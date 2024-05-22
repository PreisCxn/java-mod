package de.alive.preiscxn.impl.cytooxien;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.networking.sockets.IWebSocketConnector;
import de.alive.preiscxn.api.utils.TimeUtil;
import de.alive.preiscxn.impl.networking.sockets.WebSocketCompletion;
import de.alive.preiscxn.impl.networking.sockets.WebSocketConnector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StorageItemStack {
    private static final int REFRESH_AFTER_SECONDS = 10;
    private long lastUpdate = 0;
    private boolean setup = false;
    private final PriceText<?> priceText = PriceCxn.getMod().createPriceText(true);
    private Type type;
    private IWebSocketConnector connector;

    public StorageItemStack() {

    }

    public void setup(@NotNull JsonObject object, IWebSocketConnector connector) {
        this.type = isOf(object, Type.VENDITORPL) ? Type.VENDITORPL : Type.ITEM_STORAGE;
        this.connector = connector;
        this.setup = true;
    }

    public StorageItemStack(@NotNull JsonObject object, WebSocketConnector connector) {
        setup(object, connector);
    }
    public @NotNull Mono<Void> search(Integer storageSearchResult) {
        if (!setup) return Mono.empty();
        if (!changedStorage(storageSearchResult)) {
            return Mono.empty();
        } /*else {
            this.priceText = PriceText.create(true);
        }*/

        if (lastUpdate + REFRESH_AFTER_SECONDS * TimeUtil.TimeUnit.SECONDS.getMilliseconds() > System.currentTimeMillis())
            return Mono.empty();
        lastUpdate = System.currentTimeMillis();

        Mono<Void> searchCompletion;

        if (type.containsAmount(storageSearchResult)) {
            searchCompletion = Mono.empty();
        } else {
            searchCompletion = type.requestAmount(storageSearchResult, this.connector);
        }

        return searchCompletion.then().doOnSuccess(v -> type.getPrice(storageSearchResult).ifPresentOrElse(price -> {
            priceText.withPriceAdder(price)
                    .finishSearching();

        }, () -> {
            priceText.withPriceAdder(0)
                    .setIsSearching(PriceText.SearchingState.FAILED_SEARCHING);
        }));
    }

    public @NotNull PriceText getText() {
        return this.priceText;
    }

    public boolean changedStorage(@Nullable Integer storage) {
        return storage != null;
    }

    public static boolean isOf(@NotNull JsonObject data, @NotNull Type item) {
        if (!data.has("item_search_key") || !data.has("pbv_search_key")
                || data.get("item_search_key") == JsonNull.INSTANCE || data.get("pbv_search_key") == JsonNull.INSTANCE) return false;

        return data.get("item_search_key").getAsString().contains(item.getKey());
    }

    public static boolean isOf(@NotNull JsonObject data) {
        return isOf(data, Type.VENDITORPL) || isOf(data, Type.ITEM_STORAGE);
    }

    public enum Type {
        VENDITORPL("venditorplus", new HashMap<>(), 640, "venditorplus_price"),
        ITEM_STORAGE("item_storage", new HashMap<>(), 640, "storage_price");

        private final String key;
        private final Map<Integer, Double> priceMap;
        private final double startPrice;
        private final String query;

        Type(String key, Map<Integer, Double> priceMap, double startPrice, String query) {
            this.key = key;
            this.priceMap = priceMap;
            this.startPrice = startPrice;
            this.query = query;
        }

        public String getKey() {
            return key;
        }

        public @NotNull Optional<Double> getPrice(int amount) {
            if (amount <= this.startPrice) return Optional.of(0D);
            if (!priceMap.containsKey(amount)) return Optional.empty();
            return Optional.of(priceMap.get(amount));
        }

        public boolean containsAmount(int amount) {
            if (amount <= this.startPrice) return true;
            return priceMap.containsKey(amount);
        }

        public @NotNull Mono<Void> requestAmount(int amount, @NotNull IWebSocketConnector connector) {
            return new WebSocketCompletion(connector, this.query, String.valueOf(amount)).getMono().mapNotNull(s -> {
                if (s == null) {
                    return null;
                }

                try {
                    double price = Double.parseDouble(s);
                    priceMap.put(amount, price);
                } catch (NumberFormatException e) {
                    return null;
                }
                return null;
            });
        }

    }
}
