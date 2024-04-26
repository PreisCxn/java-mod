package de.alive.preiscxn.inventory.listener;

import com.google.gson.JsonArray;
import de.alive.api.Mod;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.cytooxien.TranslationDataAccess;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.interfaces.IScreenHandler;
import de.alive.api.listener.InventoryListener;
import de.alive.api.networking.DataAccess;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.api.LogPrinter.printDebug;
import static de.alive.api.LogPrinter.printTester;
import static de.alive.api.utils.ItemUpdater.updateItemsAsync;

public class AuctionHouseListener extends InventoryListener {

    private final List<PriceCxnItemStack> items = new ArrayList<>();
    private final Tuple2<Integer, Integer> itemRange = Tuples.of(10, 35);

    private final Map<String, DataAccess> searchData = new HashMap<>();


    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     */
    public AuctionHouseListener(@NotNull Mod mod, @NotNull DataAccess inventoryTitles, int inventorySize, AtomicBoolean... active) {
        super(mod, inventoryTitles, inventorySize <= 0 ? 6*9 : inventorySize, active);

        searchData.put("sellerName", TranslationDataAccess.SELLER_SEARCH);
        searchData.put("timestamp", TranslationDataAccess.TIMESTAMP_SEARCH);
        searchData.put("bidPrice", TranslationDataAccess.BID_SEARCH);
        searchData.put("buyPrice", TranslationDataAccess.AH_BUY_SEARCH);
        searchData.put("isBid", TranslationDataAccess.HIGHEST_BIDDER_SEARCH); //todo add isBid

    }

    public AuctionHouseListener(Mod mod, AtomicBoolean... active) {
        this(mod, TranslationDataAccess.INV_AUCTION_HOUSE_SEARCH, 0, active);
    }

    @Override
    public @NotNull Mono<Void> onInventoryOpen(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        printDebug("AuctionHouse open");

        items.clear();
        return updateItemsAsync(this.items, handler, this.itemRange, this.searchData);
    }

    @Override
    public @NotNull Mono<Void> onInventoryClose(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        printDebug("AuctionHouse close");

        JsonArray array = new JsonArray();

        if(!items.isEmpty()) {
            for (PriceCxnItemStack item : items) {
                array.add(item.getDataWithoutDisplay());
            }
        }

        if(!array.isEmpty())
            return sendData("/auctionhouse", array)
                    .doOnSuccess(aVoid -> {
                        if(client.isPlayerNull())
                            return;
                        printTester( "AuctionHouse data sent: " + array.size() + " items");
                    });
        return Mono.empty();
    }

    @Override
    public @NotNull Mono<Void> onInventoryUpdate(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        printDebug("AuctionHouse updated");
        return updateItemsAsync(this.items, handler, this.itemRange, this.searchData);
    }

}
