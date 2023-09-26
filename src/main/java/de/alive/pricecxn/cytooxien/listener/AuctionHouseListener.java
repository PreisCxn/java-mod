package de.alive.pricecxn.cytooxien.listener;

import com.google.gson.JsonArray;
import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.SearchDataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        searchData.put("buyPrice", SearchDataAccess.INSTANT_BUY_SEARCH);

    }

    public AuctionHouseListener(AtomicBoolean active) {
        this(SearchDataAccess.INV_AUCTION_HOUSE_SEARCH, 0, active);
    }

    @Override
    protected void onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("AuctionHouse open");

        items.clear();
        updateItemsAsync(this.items, handler, this.itemRange, this.searchData);
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
        updateItemsAsync(this.items, handler, this.itemRange, this.searchData);
    }

}
