package de.alive.preiscxn.fabric.v1_20_5.impl;

import de.alive.preiscxn.api.interfaces.IInventory;
import de.alive.preiscxn.api.interfaces.IItemStack;
import net.minecraft.client.MinecraftClient;

public final class InventoryImpl implements IInventory {
    private final MinecraftClient minecraftClient;

    public InventoryImpl(MinecraftClient minecraftClient) {
        this.minecraftClient = minecraftClient;
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
        return new ItemStackImpl(minecraftClient.player.getMainHandStack());
    }
}
