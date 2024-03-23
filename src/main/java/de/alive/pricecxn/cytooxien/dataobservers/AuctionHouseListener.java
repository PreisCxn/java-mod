package de.alive.pricecxn.cytooxien.dataobservers;

import com.google.gson.JsonArray;
import de.alive.pricecxn.cytooxien.CxnListener;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.TranslationDataAccess;
import de.alive.pricecxn.listener.InventoryListener;
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
    public AuctionHouseListener(@NotNull DataAccess inventoryTitles, int inventorySize, AtomicBoolean... active) {
        super(inventoryTitles, inventorySize <= 0 ? 6*9 : inventorySize, active);

        searchData.put("sellerName", TranslationDataAccess.SELLER_SEARCH);
        searchData.put("timestamp", TranslationDataAccess.TIMESTAMP_SEARCH);
        searchData.put("bidPrice", TranslationDataAccess.BID_SEARCH);
        searchData.put("buyPrice", TranslationDataAccess.AH_BUY_SEARCH);
        searchData.put("isBid", TranslationDataAccess.HIGHEST_BIDDER_SEARCH); //todo add isBid

    }

    public AuctionHouseListener(AtomicBoolean... active) {
        this(TranslationDataAccess.INV_AUCTION_HOUSE_SEARCH, 0, active);
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
                if(items.size() > 40)
                    array.add(item.getDataWithoutDisplay());
                else
                    array.add(item.getData());
            }
        }

        if(!array.isEmpty())
            sendData("/auctionhouse", array)
                    .doOnSuccess(aVoid -> printDebug("AuctionHouse data sent: " + array.size() + " items"));//todo subscribe
    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("AuctionHouse updated");
        updateItemsAsync(this.items, handler, this.itemRange, this.searchData);
    }

}
