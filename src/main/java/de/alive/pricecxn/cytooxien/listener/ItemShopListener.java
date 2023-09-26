package de.alive.pricecxn.cytooxien.listener;

import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.cytooxien.SearchDataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

public class ItemShopListener extends InventoryListener {

    private final Map<String, DataAccess> searchData = new HashMap<>();

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
    }

    @Override
    protected void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("ItemShop close");
    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        printDebug("ItemShop updated");
    }
}
