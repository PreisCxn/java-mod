package de.alive.preiscxn.listener.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.alive.preiscxn.api.Mod;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.interfaces.IScreenHandler;
import de.alive.preiscxn.api.interfaces.ISlot;
import de.alive.preiscxn.api.listener.InventoryListener;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.listener.InventoryDataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class TradeListener extends InventoryListener {
    private static final int INVENTORY_HEIGHT = 4;
    private static final int INVENTORY_WIDTH = 4;
    private static final int INVENTORY_SPACE_BETWEEN = 5;

    private final List<TradeStackRow> selfInventory = TradeStackRow.from(9, INVENTORY_HEIGHT, INVENTORY_WIDTH, INVENTORY_SPACE_BETWEEN);
    private final List<TradeStackRow> traderInventory = TradeStackRow.from(14, INVENTORY_HEIGHT, INVENTORY_WIDTH, INVENTORY_SPACE_BETWEEN);
    private final TradeStackRow selfControls = TradeStackRow.from(1, 2);
    private final TradeStackRow traderControls = TradeStackRow.from(6, 2);
    private final Map<String, DataAccess> searchData = new HashMap<>();

    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     */
    public TradeListener(@NotNull Mod mod, @NotNull DataAccess inventoryTitles, int inventorySize, @Nullable AtomicBoolean... active) {
        super(mod, inventoryTitles, inventorySize <= 0 ? 6 * 9 : inventorySize, active);

        this.searchData.put("buyPrice", InventoryDataAccess.TRADE_BUY_SEARCH);
    }

    public TradeListener(Mod mod, @Nullable AtomicBoolean... active) {
        this(mod, InventoryDataAccess.INV_TRADE_SEARCH, 0, active);
    }

    @Override
    public @NotNull Mono<Void> onInventoryOpen(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        PriceCxn.getMod().printDebug("Trade open");

        return Flux.concat(
                Flux.fromIterable(selfInventory)
                        .concatMap(row -> row.updateAsync(handler, null, true)),
                Flux.fromIterable(traderInventory)
                        .concatMap(row -> row.updateAsync(handler, null, true)),
                selfControls.updateAsync(handler, this.searchData, false),
                traderControls.updateAsync(handler, this.searchData, false)
        ).then();

    }

    @Override
    public @NotNull Mono<Void> onInventoryClose(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        PriceCxn.getMod().printDebug("Trade close");

        JsonObject array = new JsonObject();
        array.add("self", TradeStackRow.getData(selfInventory));
        array.add("trader", TradeStackRow.getData(traderInventory));
        array.add("selfControls", selfControls.getData());
        array.add("traderControls", traderControls.getData());

        Optional<JsonElement> result = processData(TradeStackRow.getItemStacks(selfInventory),
                TradeStackRow.getItemStacks(traderInventory),
                selfControls.getData(),
                traderControls.getData());

        PriceCxn.getMod().printTester(result.isPresent() ? result.get().toString() : "Failed to get result");

        return result.map(jsonElement -> sendData("/trade", jsonElement)).orElse(Mono.empty());

    }

    @Override
    public @NotNull Mono<Void> onInventoryUpdate(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        PriceCxn.getMod().printDebug("Trade updated");
        return Flux.concat(
                Flux.fromIterable(selfInventory)
                        .concatMap(row -> row.updateAsync(handler, null, true)),
                Flux.fromIterable(traderInventory)
                        .concatMap(row -> row.updateAsync(handler, null, true)),
                selfControls.updateAsync(handler, this.searchData, false),
                traderControls.updateAsync(handler, this.searchData, false)
        ).then();
    }

    private @NotNull Optional<JsonElement> processData(@NotNull List<PriceCxnItemStack> selfInv,
                                                       @NotNull List<PriceCxnItemStack> traderInv,
                                                       @NotNull JsonArray selfControls,
                                                       @NotNull JsonArray traderControls) {
        if (selfControls.isEmpty() || traderControls.isEmpty()) return Optional.empty();
        if (selfInv.isEmpty() == traderInv.isEmpty())
            return Optional.empty(); //return empty wenn beide empty oder beide nicht empty
        if (notAccepted(traderControls) && notAccepted(selfControls)) return Optional.empty();

        List<PriceCxnItemStack> items;
        Optional<String> price;

        if (selfInv.isEmpty()) {
            if (buyPriceIsNull(selfControls) || !buyPriceIsNull(traderControls)) return Optional.empty();
            items = traderInv;
            price = getBuyPrice(selfControls).map(JsonElement::getAsString);
        } else if (traderInv.isEmpty()) {
            if (buyPriceIsNull(traderControls) || !buyPriceIsNull(selfControls)) return Optional.empty();
            items = selfInv;
            price = getBuyPrice(traderControls).map(JsonElement::getAsString);
        } else return Optional.empty();

        int amount = items.getFirst().getAmount();

        for (int i = 1; i < items.size(); i++) {
            if (!items.get(i).isSameItem(items.getFirst())) return Optional.empty();
            amount += items.get(i).getAmount();
        }

        JsonObject result = items.getFirst().getDataWithoutDisplay();

        result.remove(PriceCxnItemStack.AMOUNT_KEY);
        result.addProperty(PriceCxnItemStack.AMOUNT_KEY, amount);
        price.ifPresent(s -> result.addProperty("buyPrice", s));

        PriceCxn.getMod().getLogger().debug(result.toString());

        return result.isJsonNull() ? Optional.empty() : Optional.of(result);
    }

    private boolean buyPriceIsNull(@NotNull JsonArray array) {
        Optional<JsonElement> get = getBuyPrice(array);
        return get.map(jsonElement -> jsonElement.getAsString().equals("0,00") || jsonElement.getAsString().equals("0.00")).orElse(false);
    }

    private @NotNull Optional<JsonElement> getBuyPrice(@NotNull JsonArray array) {
        return array.asList()
                .stream()
                .filter(JsonElement::isJsonObject)
                .filter(e -> e.getAsJsonObject().get("itemName").getAsString().equals("block.minecraft.player_head"))
                .findFirst()
                .map(e -> e.getAsJsonObject().get("buyPrice"))
                .filter(jsonElement -> !jsonElement.isJsonNull());
    }

    private boolean notAccepted(@NotNull JsonArray array) {
        Optional<JsonElement> element = array.asList()
                .stream()
                .filter(JsonElement::isJsonObject)
                .filter(e -> e.getAsJsonObject().get("itemName").getAsString().equals("block.minecraft.lime_concrete"))
                .findFirst();

        return element.isEmpty();
    }

    private record TradeStackRow(Tuple2<Integer, Integer> slotRange, List<PriceCxnItemStack> slots) {

        static @NotNull List<TradeStackRow> from(int startValue, int height, int width, int spaceBetween) {
            List<TradeStackRow> result = new ArrayList<>(height * width);

            for (int i = 0; i < height; i++) {
                int start = startValue + i * (width + spaceBetween);
                result.add(from(start, width));
            }

            return result;
        }

        static @NotNull TradeStackRow from(int start, int width) {
            return new TradeStackRow(Tuples.of(start, start + width - 1), new ArrayList<>());
        }

        static @NotNull JsonArray getData(@NotNull List<TradeStackRow> rows) {
            JsonArray array = new JsonArray();
            for (TradeStackRow row : rows) {
                array.addAll(row.getData());
            }
            return array;
        }

        static @NotNull List<PriceCxnItemStack> getItemStacks(@NotNull List<TradeStackRow> rows) {
            List<PriceCxnItemStack> result = new ArrayList<>();
            for (TradeStackRow row : rows) {
                result.addAll(row.slots);
            }
            return result;
        }

        void update(@NotNull IScreenHandler handler, @Nullable Map<String, DataAccess> searchData, boolean bool) {
            synchronized (slots) {
                slots.clear();
                for (int i = slotRange.getT1(); i <= slotRange.getT2(); i++) {
                    ISlot slot = handler.getSlot(i);
                    if (slot.isStackEmpty()) continue;

                    PriceCxnItemStack newItem = slot.createItemStack(searchData, bool);

                    slots.add(newItem);
                }
            }
        }

        @NotNull Mono<Void> updateAsync(@NotNull IScreenHandler handler, @Nullable Map<String, DataAccess> searchData, boolean bool) {
            return Mono.fromRunnable(() -> update(handler, searchData, bool)).then();
        }

        @NotNull JsonArray getData() {
            JsonArray array = new JsonArray();
            for (PriceCxnItemStack item : slots) {
                array.add(item.getDataWithoutDisplay());
            }
            return array;
        }

    }

}