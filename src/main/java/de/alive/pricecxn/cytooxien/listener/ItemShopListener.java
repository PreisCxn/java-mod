package de.alive.pricecxn.cytooxien.listener;

import de.alive.pricecxn.PriceCxnMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.alive.pricecxn.PriceCxnMod.printDebug;

public class ItemShopListener extends InventoryListener {
    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     * @param active
     */
    public ItemShopListener(@Nullable List<String> inventoryTitles, int inventorySize, @Nullable AtomicBoolean active) {
        super(inventoryTitles == null ? List.of("Spieler-Shop") : inventoryTitles, inventorySize <= 0 ? 3*9 : inventorySize, active);
    }

    public ItemShopListener(@Nullable AtomicBoolean active) {
        this(null, 0, active);
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
