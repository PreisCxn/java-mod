package de.alive.pricecxn.cytooxien.listener;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.SearchDataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

public class TradeListener extends InventoryListener {

    private static final int INVENTORY_HEIGHT = 4;
    private static final int INVENTORY_WIDTH = 4;
    private static final int INVENTORY_SPACE_BETWEEN = 5;

    private final List<TradeStackRow> selfInventory = TradeStackRow.from(9, INVENTORY_HEIGHT, INVENTORY_WIDTH, INVENTORY_SPACE_BETWEEN);
    private final List<TradeStackRow> traderInventory = TradeStackRow.from(14, INVENTORY_HEIGHT, INVENTORY_WIDTH, INVENTORY_SPACE_BETWEEN);
    private final TradeStackRow selfControls = TradeStackRow.from(1,2);
    private final TradeStackRow traderControls = TradeStackRow.from(6,2);
    private final Map<String, DataAccess> searchData = new HashMap<>();

    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     * @param active
     */
    public TradeListener(@NotNull DataAccess inventoryTitles, int inventorySize, @Nullable AtomicBoolean active) {
        super(inventoryTitles, inventorySize <= 0 ? 6*9 : inventorySize, active);

        this.searchData.put("buyPrice", SearchDataAccess.TRADE_BUY_SEARCH);
    }

    public TradeListener(@Nullable AtomicBoolean active) {
        this(SearchDataAccess.INV_TRADE_SEARCH, 0, active);
    }

    @Override
    protected void onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("Trade open");
        selfInventory.forEach(row -> row.updateAsync(handler, null, true));
        traderInventory.forEach(row -> row.updateAsync(handler, null, true));
        selfControls.updateAsync(handler, this.searchData, false);
        traderControls.updateAsync(handler, this.searchData, false);
    }

    @Override
    protected void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("Trade close");

        JsonObject array = new JsonObject();
        array.add("self",TradeStackRow.getData(selfInventory));
        array.add("trader",TradeStackRow.getData(traderInventory));
        array.add("selfControls",selfControls.getData());
        array.add("traderControls",traderControls.getData());

        System.out.println(array);

    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("Trade updated");
        selfInventory.forEach(row -> row.updateAsync(handler, null, true));
        traderInventory.forEach(row -> row.updateAsync(handler, null, true));
        selfControls.updateAsync(handler, this.searchData, false);
        traderControls.updateAsync(handler, this.searchData, false);
    }

    private record TradeStackRow(Pair<Integer, Integer> slotRange, List<PriceCxnItemStack> slots) {

        static List<TradeStackRow> from(int startValue, int height, int width, int spaceBetween) {
            List<TradeStackRow> result = new ArrayList<>(height * width);

            for (int i = 0; i < height; i++) {
                int start = startValue + i * (width + spaceBetween);
                result.add(from(start, width));
            }

            return result;
        }

        static List<TradeStackRow> from(int startValue) {
            return from(startValue, INVENTORY_HEIGHT, INVENTORY_WIDTH, INVENTORY_SPACE_BETWEEN);
        }

        static TradeStackRow from(int start, int width) {
            return new TradeStackRow(new Pair<>(start, start + width - 1), new ArrayList<>());
        }

        void update(@NotNull ScreenHandler handler, @Nullable Map<String, DataAccess> searchData, boolean bool) {
            synchronized (slots) {
                slots.clear();
                for (int i = slotRange.getLeft(); i <= slotRange.getRight(); i++) {
                    Slot slot = handler.getSlot(i);
                    if (slot.getStack().isEmpty()) continue;

                    PriceCxnItemStack newItem = new PriceCxnItemStack(slot.getStack(), searchData, bool);

                    slots.add(newItem);
                }
            }
        }

        void updateAsync(@NotNull ScreenHandler handler, @Nullable Map<String, DataAccess> searchData, boolean bool){
            CompletableFuture.runAsync(() -> update(handler, searchData, bool), EXECUTOR);
        }

        JsonArray getData() {
            JsonArray array = new JsonArray();
            for(PriceCxnItemStack item : slots) {
                array.add(item.getData());
            }
            return array;
        }

        static JsonArray getData(List<TradeStackRow> rows) {
            JsonArray array = new JsonArray();
            for (TradeStackRow row : rows) {
                array.addAll(row.getData());
            }
            return array;
        }
    }
}
