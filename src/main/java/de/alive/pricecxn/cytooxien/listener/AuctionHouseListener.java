package de.alive.pricecxn.cytooxien.listener;

import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.SearchDataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
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

public class AuctionHouseListener extends InventoryListener {

    private final List<PriceCxnItemStack> items = new ArrayList<>();

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
        items.add(new PriceCxnItemStack(handler.getSlot(34).getStack(), this.searchData));
        items.add(new PriceCxnItemStack(handler.getSlot(34).getStack(), this.searchData));

    }

    @Override
    protected void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("AuctionHouse close");
        if(!items.isEmpty()) {
            System.out.println(items.get(0).getData());
        }
    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("AuctionHouse updated");

        items.clear();
        items.add(new PriceCxnItemStack(handler.getSlot(34).getStack(), this.searchData));
        items.add(new PriceCxnItemStack(handler.getSlot(34).getStack(), this.searchData));
    }

    private @NotNull CompletableFuture<Pair<Boolean, List<Integer>>> hadItemChange(@NotNull ScreenHandler handler, @Nullable Pair<Integer, Integer> slotRange) {

        return CompletableFuture.supplyAsync(() -> {
            Pair<Integer, Integer> slots = slotRange == null ? new Pair<>(0, handler.slots.size()) : slotRange;

            for(int i = slots.getLeft(); i < slots.getRight(); i++) {
                if(handler.getSlot(i).getStack().isEmpty()) continue;
                if(handler.getSlot(i).getStack().getItem().getTranslationKey().equals("item.minecraft.air")) continue;
                if(handler.getSlot(i).getStack().getCount() == 0) continue;


            }

            return new Pair<>(false, new ArrayList<>());
        }, EXECUTOR);
    }

}
