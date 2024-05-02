package de.alive.preiscxn.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.alive.api.interfaces.IInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.concurrent.ExecutionException;

public class InventoryImpl implements IInventory {
    private static final Cache<Tuple2<MinecraftClient, Inventory>, InventoryImpl> inventoryMap = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .build();
    private final MinecraftClient minecraftClient;
    private final Inventory inventory;

    private InventoryImpl(MinecraftClient minecraftClient, Inventory inventory) {
        this.minecraftClient = minecraftClient;
        this.inventory = inventory;
    }

    public static InventoryImpl getInstance(MinecraftClient minecraftClient, Inventory inventory) {
        try {
            return inventoryMap.get(Tuples.of(minecraftClient, inventory), () -> new InventoryImpl(minecraftClient, inventory));
        } catch (ExecutionException e) {
            return new InventoryImpl(minecraftClient, inventory);
        }
    }

    @Override
    public String getTitle() {
        return minecraftClient.currentScreen == null ? null : minecraftClient.currentScreen.getTitle().getString();
    }

    @Override
    public int getSize() {
        if(minecraftClient.player == null || minecraftClient.player.currentScreenHandler == null || minecraftClient.player.currentScreenHandler.getSlot(0) == null)
            return 0;

        return minecraftClient.player == null ? 0 : minecraftClient.player.currentScreenHandler.getSlot(0).inventory.size();
    }
}
