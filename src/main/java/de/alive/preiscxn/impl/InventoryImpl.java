package de.alive.preiscxn.impl;

import de.alive.api.interfaces.IInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;

public class InventoryImpl implements IInventory {
    private final MinecraftClient minecraftClient;
    private final Inventory inventory;

    public InventoryImpl(MinecraftClient minecraftClient, Inventory inventory) {
        this.minecraftClient = minecraftClient;
        this.inventory = inventory;
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
