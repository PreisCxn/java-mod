package de.alive.pricecxn.cytooxien.listener;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuctionHouseListener extends InventoryListener {
    /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     */
    public AuctionHouseListener(@Nullable List<String> inventoryTitles, int inventorySize, AtomicBoolean active) {
        super(inventoryTitles == null ? List.of("Auktionshaus") : inventoryTitles, inventorySize <= 0 ? 6*9 : inventorySize, active);
    }

    public AuctionHouseListener(AtomicBoolean active) {
        this(null, 0, active);
    }

    @Override
    protected void onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        System.out.println("AuctionHouseListener.onInventoryOpen");
    }

    @Override
    protected void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        System.out.println("AuctionHouseListener.onInventoryClose");
    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        System.out.println("AuctionHouseListener.onInventoryUpdate");
    }
}
