package de.alive.pricecxn.cytooxien.listener;

import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AuctionHouseListener extends InventoryListener {

    private final List<PriceCxnItemStack> itemStacks = new ArrayList<>();


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
        PriceCxnMod.printDebug("AuctionHouseListener.onInventoryOpen", true);

        itemStacks.clear();

    }

    @Override
    protected void onInventoryClose(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        PriceCxnMod.printDebug("AuctionHouseListener.onInventoryClose", true);
    }

    @Override
    protected void onInventoryUpdate(@NotNull MinecraftClient client, @NotNull ScreenHandler handler) {
        PriceCxnMod.printDebug("AuctionHouseListener.onInventoryOpen", true);
    }

}
