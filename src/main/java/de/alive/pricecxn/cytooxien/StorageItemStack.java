package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.alive.pricecxn.networking.sockets.WebSocketCompletion;
import de.alive.pricecxn.networking.sockets.WebSocketConnector;
import de.alive.pricecxn.utils.TimeUtil;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StorageItemStack {
    private static final int REFRESH_AFTER_SECONDS = 10;
    private long lastUpdate = 0;
    private boolean setup = false;
    private final PriceText priceText = PriceText.create(true);
    private Type type;
    private WebSocketConnector connector;

    public StorageItemStack() {

    }

    public void setup(JsonObject object, WebSocketConnector connector){
        this.type = isOf(object, Type.VENDITORPL) ? Type.VENDITORPL : Type.ITEM_STORAGE;
        this.connector = connector;
        this.setup = true;
    }

    public StorageItemStack(JsonObject object, WebSocketConnector connector) {
        setup(object, connector);
    }
    public Mono<Void> search(Integer storageSearchResult) {
        if(!setup) return null;
        if(changedStorage(storageSearchResult)) {
            //this.priceText = PriceText.create(true);
        } else return null;

        if(lastUpdate + REFRESH_AFTER_SECONDS * TimeUtil.TimeUnit.SECONDS.getMilliseconds() > System.currentTimeMillis())
            return null;
        lastUpdate = System.currentTimeMillis();

        Mono<Void> searchCompletion;

        if(type.containsAmount(storageSearchResult)) {
            searchCompletion = Mono.empty();
        } else {
            searchCompletion = type.requestAmount(storageSearchResult, this.connector);
        }

        return searchCompletion.then().doOnSuccess(v -> {

            type.getPrice(storageSearchResult).ifPresentOrElse(price -> {
                priceText.withPriceAdder(price);
                priceText.finishSearching();

            }, () -> {
                priceText.withPriceAdder(0);
                priceText.setIsSearching(PriceText.SearchingState.FAILED_SEARCHING);
            });
        });
    }

    public PriceText getText() {
        return this.priceText;
    }

    public boolean changedStorage(Integer storage) {
        return storage != null;
    }

    public static boolean isOf(JsonObject data, Type item) {
        if(!data.has("item_search_key") || !data.has("pbv_search_key")
                || data.get("item_search_key") == JsonNull.INSTANCE || data.get("pbv_search_key") == JsonNull.INSTANCE) return false;

        return data.get("item_search_key").getAsString().contains(item.getKey());
    }

    public static boolean isOf(JsonObject data) {
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

        public Optional<Double> getPrice(int amount) {
            if(amount <= this.startPrice) return Optional.of(0D);
            if(!priceMap.containsKey(amount)) return Optional.empty();
            return Optional.of(priceMap.get(amount));
        }

        public boolean containsAmount(int amount) {
            if(amount <= this.startPrice) return true;
            return priceMap.containsKey(amount);
        }

        public Mono<Void> requestAmount(int amount, WebSocketConnector connector) {
            return new WebSocketCompletion(connector, this.query, String.valueOf(amount)).getMono().mapNotNull(s -> {
                if (s == null) {
                    return null;
                }

                try {
                    double price = Double.parseDouble(s);
                    priceMap.put(amount, price);
                }catch(NumberFormatException e){
                    return null;
                }
                return null;
            });
        }

    }
}
