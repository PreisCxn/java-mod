package de.alive.preiscxn.fabric.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.alive.preiscxn.api.interfaces.IInventory;
import de.alive.preiscxn.api.interfaces.IItemStack;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.ExecutionException;

public final class InventoryImpl implements IInventory {
    private static final Cache<MinecraftClient, InventoryImpl> INVENTORY_MAP
            = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .build();
    private final MinecraftClient minecraftClient;

    private InventoryImpl(MinecraftClient minecraftClient) {
        this.minecraftClient = minecraftClient;
    }

    public static InventoryImpl getInstance(MinecraftClient minecraftClient) {
        try {
            return INVENTORY_MAP.get(minecraftClient, () -> new InventoryImpl(minecraftClient));
        } catch (ExecutionException e) {
            return new InventoryImpl(minecraftClient);
        }
    }

    @Override
    public String getTitle() {
        return minecraftClient.currentScreen == null ? null : minecraftClient.currentScreen.getTitle().getString();
    }

    @Override
    public int getSize() {
        if (minecraftClient.player == null
            || minecraftClient.player.currentScreenHandler == null
            || minecraftClient.player.currentScreenHandler.getSlot(0) == null)
            return 0;

        return minecraftClient.player == null ? 0 : minecraftClient.player.currentScreenHandler.getSlot(0).inventory.size();
    }

    @Override
    public IItemStack getMainHandStack() {
        if(minecraftClient.player == null)
            return null;
        return ItemStackImpl.getInstance(minecraftClient.player.getMainHandStack());
    }
}
