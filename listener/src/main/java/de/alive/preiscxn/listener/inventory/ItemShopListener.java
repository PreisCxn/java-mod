package de.alive.preiscxn.listener.inventory;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.alive.preiscxn.api.Mod;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.interfaces.IScreenHandler;
import de.alive.preiscxn.api.listener.InventoryListener;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.listener.InventoryDataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.preiscxn.api.LogPrinter.printDebug;
import static de.alive.preiscxn.api.LogPrinter.printTester;
import static de.alive.preiscxn.api.utils.ItemUpdater.updateItem;

public class ItemShopListener extends InventoryListener {

    private final Map<String, DataAccess> searchData = new HashMap<>();
    private final int itemStackSlot = 13;
    private final int buyItemSlot = 11;
    private final int sellItemSlot = 15;
    private @Nullable PriceCxnItemStack itemStack = null;

    private @Nullable PriceCxnItemStack buyItem = null;

    private @Nullable PriceCxnItemStack sellItem = null;

    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     * @param active
     */
    public ItemShopListener(@NotNull Mod mod, @NotNull DataAccess inventoryTitles, int inventorySize, @Nullable AtomicBoolean... active) {
        super(mod, inventoryTitles, inventorySize <= 0 ? 3 * 9 : inventorySize, active);

        searchData.put("buyPrice", InventoryDataAccess.SHOP_BUY_SEARCH);
        searchData.put("sellPrice", InventoryDataAccess.SHOP_SELL_SEARCH);

    }

    public ItemShopListener(Mod mod, @Nullable AtomicBoolean... active) {
        this(mod, InventoryDataAccess.INV_ITEM_SHOP_SEARCH, 0, active);
    }

    @Override
    public @NotNull Mono<Void> onInventoryOpen(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        printDebug("ItemShop open");

        itemStack = null;
        buyItem = null;
        sellItem = null;
        return updateItemStacks(handler);

    }

    @Override
    public @NotNull Mono<Void> onInventoryClose(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        printDebug("ItemShop close");
        if (sellItem == null && buyItem == null || itemStack == null) return Mono.empty();

        JsonObject object = itemStack.getData();

        if (!buyItem.getData().has("sellPrice") && !sellItem.getData().has("buyPrice")) return Mono.empty();

        JsonElement buyItemE = buyItem.getData().get("buyPrice");
        JsonElement sellItemE = sellItem.getData().get("sellPrice");

        if (buyItemE == JsonNull.INSTANCE && sellItemE == JsonNull.INSTANCE) return Mono.empty();

        object.add("sellPrice", sellItem.getData().get("sellPrice"));
        object.add("buyPrice", buyItem.getData().get("buyPrice"));

        return sendData("/itemshop", object).doOnSuccess(aVoid -> printTester("ItemShop data sent"));
    }

    @Override
    public @NotNull Mono<Void> onInventoryUpdate(@NotNull IMinecraftClient client, @NotNull IScreenHandler handler) {
        printDebug("ItemShop updated");
        return updateItemStacks(handler);
    }

    private @NotNull Mono<Void> updateItemStacks(@NotNull IScreenHandler handler) {
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
