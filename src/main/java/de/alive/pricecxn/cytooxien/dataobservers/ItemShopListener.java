package de.alive.pricecxn.cytooxien.dataobservers;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.TranslationDataAccess;
import de.alive.pricecxn.listener.InventoryListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

public class ItemShopListener extends InventoryListener {

    private final Map<String, DataAccess> searchData = new HashMap<>();
    private final int itemStackSlot = 13;
    private final int buyItemSlot = 11;
    private final int sellItemSlot = 15;
    private PriceCxnItemStack itemStack = null;

    private PriceCxnItemStack buyItem = null;

    private PriceCxnItemStack sellItem = null;

    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     * @param active
     */
    public ItemShopListener(@NotNull DataAccess inventoryTitles, int inventorySize, @Nullable AtomicBoolean... active) {
        super(inventoryTitles, inventorySize <= 0 ? 3*9 : inventorySize, active);

        searchData.put("buyPrice", TranslationDataAccess.SHOP_BUY_SEARCH);
        searchData.put("sellPrice", TranslationDataAccess.SHOP_SELL_SEARCH);

    }

    public ItemShopListener(@Nullable AtomicBoolean... active) {
        this(TranslationDataAccess.INV_ITEM_SHOP_SEARCH, 0, active);
    }

    @Override
    protected void onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("ItemShop open");

        itemStack = null;
        buyItem = null;
        sellItem = null;
        updateItemStacks(handler);//todo subscribe

    }

    @Override
    protected void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("ItemShop close");
        if((sellItem == null && buyItem == null) || itemStack == null) return;

        JsonObject object = itemStack.getData();

        if(!buyItem.getData().has("sellPrice") && !sellItem.getData().has("buyPrice")) return;

        JsonElement buyItemE =  buyItem.getData().get("buyPrice");
        JsonElement sellItemE =  sellItem.getData().get("sellPrice");

        if(buyItemE == JsonNull.INSTANCE && sellItemE == JsonNull.INSTANCE) return;

        object.add("sellPrice",  sellItem.getData().get("sellPrice"));
        object.add("buyPrice",  buyItem.getData().get("buyPrice"));

        sendData("/itemshop", object).doOnSuccess(aVoid -> {
            printDebug("ItemShop data sent");
        });//todo subscribe;


    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("ItemShop updated");
        updateItemStacks(handler);//todo subscribe
    }

    private Mono<Void> updateItemStacks(@NotNull ScreenHandler handler){
        return Mono.fromRunnable(() -> {
            //middleItem
            Optional<PriceCxnItemStack> middle = updateItem(itemStack, handler, itemStackSlot);
            middle.ifPresent(priceCxnItemStack -> itemStack = priceCxnItemStack);

            //buyItem
            Optional<PriceCxnItemStack> buy = updateItem(itemStack, handler, buyItemSlot, this.searchData, false);
            buy.ifPresent(priceCxnItemStack -> buyItem = priceCxnItemStack);

            //sellItem
            Optional<PriceCxnItemStack> sell = updateItem(itemStack, handler, sellItemSlot, this.searchData, false);
            sell.ifPresent(priceCxnItemStack -> sellItem = priceCxnItemStack);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

}
