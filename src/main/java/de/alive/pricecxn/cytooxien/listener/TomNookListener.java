package de.alive.pricecxn.cytooxien.listener;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TomNookListener extends InventoryListener {
     /**
     * This constructor is used to listen to a specific inventory
     *
     * @param inventoryTitles The titles of the inventories to listen to
     * @param inventorySize   The size of the inventories to listen to (in slots)
     * @param active
     */
    public TomNookListener(@Nullable List<String> inventoryTitles, int inventorySize, @Nullable AtomicBoolean active) {
        super(inventoryTitles == null ? List.of("Shop") : inventoryTitles, inventorySize <= 0 ? 1*9 : inventorySize, active);
    }

    public TomNookListener(@Nullable AtomicBoolean active) {
        this(null, 0, active);
    }

    @Override
    protected void onInventoryOpen(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        System.out.println("TomNookListener.onInventoryOpen");
    }

    @Override
    protected void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        System.out.println("TomNookListener.onInventoryClose");
    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        System.out.println("TomNookListener.onInventoryUpdate");
    }
}
