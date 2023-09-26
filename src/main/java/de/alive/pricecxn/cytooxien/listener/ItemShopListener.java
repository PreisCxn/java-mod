package de.alive.pricecxn.cytooxien.listener;

import com.google.gson.JsonObject;
import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.SearchDataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

public class ItemShopListener extends InventoryListener {

    private final List<PriceCxnItemStack> items = new ArrayList<>();
    private final Map<String, DataAccess> searchData = new HashMap<>();
    private final int itemStackSlot = 13;
    private final int buyItemSlot = 11;
    private final int sellItemSlot = 15;
    private PriceCxnItemStack itemStack;

    private PriceCxnItemStack buyItem;

    private PriceCxnItemStack sellItem;

    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     * @param active
     */
    public ItemShopListener(@NotNull DataAccess inventoryTitles, int inventorySize, @Nullable AtomicBoolean active) {
        super(inventoryTitles, inventorySize <= 0 ? 3*9 : inventorySize, active);

        searchData.put("buyPrice", SearchDataAccess.SHOP_BUY_SEARCH);
        searchData.put("sellPrice", SearchDataAccess.SHOP_SELL_SEARCH);

    }

    public ItemShopListener(@Nullable AtomicBoolean active) {
        this(SearchDataAccess.INV_ITEM_SHOP_SEARCH, 0, active);
    }

    @Override
    protected void onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("ItemShop open");

        items.clear();
        updateItemStacks(handler);

    }

    @Override
    protected void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("ItemShop close");

        JsonObject object = itemStack.getData();

        object.add("sellPrice",  sellItem.getData().get("sellPrice"));
        object.add("buyPrice",  buyItem.getData().get("buyPrice"));

        System.out.println(object);

    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("ItemShop updated");
        updateItemStacks(handler);
    }

    private void updateItemStacks(@NotNull ScreenHandler handler){
        CompletableFuture.runAsync(() -> {

            //middleItem
            Optional<PriceCxnItemStack> middle = updateItem(itemStack, handler, itemStackSlot);
            middle.ifPresent(priceCxnItemStack -> itemStack = priceCxnItemStack);

            //buyItem
            Optional<PriceCxnItemStack> buy = updateItem(itemStack, handler, buyItemSlot, this.searchData, false);
            buy.ifPresent(priceCxnItemStack -> buyItem = priceCxnItemStack);

            //sellItem
            Optional<PriceCxnItemStack> sell = updateItem(itemStack, handler, sellItemSlot, this.searchData, false);
            sell.ifPresent(priceCxnItemStack -> sellItem = priceCxnItemStack);

        }, EXECUTOR);
    }

}
