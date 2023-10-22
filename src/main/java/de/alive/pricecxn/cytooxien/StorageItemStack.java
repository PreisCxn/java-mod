package de.alive.pricecxn.cytooxien;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.alive.pricecxn.networking.sockets.WebSocketCompletion;
import de.alive.pricecxn.networking.sockets.WebSocketConnector;
import de.alive.pricecxn.utils.TimeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class StorageItemStack {
    private static final int REFRESH_AFTER_SECONDS = 10;
    private CompletableFuture<Void> searchCompletion = null;
    private long lastUpdate = 0;
    private boolean setup = false;
    private Integer storageSearchResult;
    private PriceText priceText = PriceText.create(true);
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
    public void search(Integer storageSearchResult) {
        if(!setup) return;
        if(changedStorage(storageSearchResult)) {
            //this.priceText = PriceText.create(true);
        } else return;
        if(isSearching()) return;
        if(lastUpdate + REFRESH_AFTER_SECONDS * TimeUtil.TimeUnit.SECONDS.getMilliseconds() > System.currentTimeMillis()) return;
        lastUpdate = System.currentTimeMillis();

        searchCompletion = new CompletableFuture<>();

        if(type.containsAmount(storageSearchResult)) {
            searchCompletion.complete(null);
        } else
            searchCompletion = type.requestAmount(storageSearchResult, this.connector);

        searchCompletion.thenRun(() -> {
            System.out.println("finished");
            type.getPrice(storageSearchResult).ifPresentOrElse(price -> {
                priceText.withPriceAdder(price);
                priceText.finishSearching();
                System.out.println("Price: " + price);
            }, () -> {
                System.out.println("Price2: " + storageSearchResult);
                priceText.withPriceAdder(0);
                priceText.setIsSearching(PriceText.SearchingState.FAILED_SEARCHING);
            });
        });
    }

    public PriceText getText() {
        return this.priceText;
    }

    private boolean isSearching() {
        if(searchCompletion == null) return false;

        return searchCompletion.isDone();
    }

    public boolean changedStorage(Integer storage) {
        return Optional.ofNullable(storage)
                .map(value -> {
                    Optional.ofNullable(storageSearchResult).map(integer -> !value.equals(integer));
                    return true;
                })
                .orElse(false);
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

        public CompletableFuture<Void> requestAmount(int amount, WebSocketConnector connector) {

            CompletableFuture<Void> future = new CompletableFuture<>();

            new WebSocketCompletion(connector, this.query, String.valueOf(amount)).getFuture().thenCompose(s -> {
                if(s == null) {
                    future.complete(null);
                    return null;
                }
                try {
                    double price = Double.parseDouble(s);
                    priceMap.put(amount, price);
                } catch (NumberFormatException e) {
                    future.complete(null);
                    return null;
                }
                future.complete(null);
                return null;

            });

            return future;
        }

    }
}
