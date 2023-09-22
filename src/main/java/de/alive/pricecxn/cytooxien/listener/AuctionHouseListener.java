package de.alive.pricecxn.cytooxien.listener;

import com.google.gson.JsonArray;
import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.SearchDataAccess;
import de.alive.pricecxn.utils.TimeUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

public class AuctionHouseListener extends InventoryListener {

    private final List<PriceCxnItemStack> items = new ArrayList<>();
    private final Pair<Integer, Integer> itemRange = new Pair<>(10, 35);

    private final Map<String, DataAccess> searchData = new HashMap<>();


    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     */
    public AuctionHouseListener(@NotNull DataAccess inventoryTitles, int inventorySize, AtomicBoolean active) {
        super(inventoryTitles, inventorySize <= 0 ? 6*9 : inventorySize, active);

        searchData.put("sellerName", SearchDataAccess.SELLER_SEARCH);
        searchData.put("timestamp", SearchDataAccess.TIMESTAMP_SEARCH);
        searchData.put("bidPrice", SearchDataAccess.BID_SEARCH);
        searchData.put("buyPrice", SearchDataAccess.BUY_SEARCH);

    }

    public AuctionHouseListener(AtomicBoolean active) {
        this(SearchDataAccess.INV_AUCTION_HOUSE_SEARCH, 0, active);
    }

    @Override
    protected void onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("AuctionHouse open");

        items.clear();
        updateItemsAsync(handler, this.itemRange);
    }

    @Override
    protected void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("AuctionHouse close");

        JsonArray array = new JsonArray();

        if(!items.isEmpty()) {
            for (PriceCxnItemStack item : items) {
                array.add(item.getData());
            }
        }

        System.out.println("AuctionHouse: " + array.size() + " items");
        System.out.println(array);
    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("AuctionHouse updated");
        updateItemsAsync(handler, this.itemRange);
    }

    private void updateItemsAsync(ScreenHandler handler, Pair<Integer, Integer> range){
        CompletableFuture.supplyAsync(() -> {
            for (int i = range.getLeft(); i < range.getRight(); i++) {
                Slot slot = handler.getSlot(i);
                if (slot.getStack().isEmpty()) continue;

                PriceCxnItemStack newItem = new PriceCxnItemStack(slot.getStack(), this.searchData);

                boolean add = true;

                synchronized (this.items) {
                    for (PriceCxnItemStack item : items) {
                        if (item.equals(newItem)) {
                            if (!TimeUtil.timestampsEqual(item.getData().get("timestamp").getAsLong(), newItem.getData().get("timestamp").getAsLong(), 5))
                                continue;

                            add = false;
                            if (!item.deepEquals(newItem)) {
                                if (!item.getData().get("timestamp").equals(newItem.getData().get("timestamp"))) {
                                    item.getData().remove("timestamp");
                                    item.getData().add("timestamp", newItem.getData().get("timestamp"));
                                }
                                if (!item.getData().get("bidPrice").equals(newItem.getData().get("bidPrice"))) {
                                    item.getData().remove("bidPrice");
                                    item.getData().add("bidPrice", newItem.getData().get("bidPrice"));
                                }
                            }
                            break;
                        }
                    }

                    if (add)
                        this.items.add(newItem);
                }

            }

            return null;
        }, EXECUTOR);

    }

}
