package de.alive.preiscxn.v1_20_5.impl;

import de.alive.api.interfaces.IInventory;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.interfaces.IScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public final class MinecraftClientImpl implements IMinecraftClient {
    private final Minecraft minecraftClient;

    public MinecraftClientImpl(Minecraft minecraftClient) {
        this.minecraftClient = minecraftClient;
    }

    @Override
    public boolean isPlayerNull() {
        return minecraftClient.player == null;
    }

    @Override
    public boolean isCurrentScreenNull() {
        return minecraftClient.screen == null;
    }

    @Override
    public boolean isCurrentScreenHandlerNull() {
        return minecraftClient.player == null;
    }

    @Override
    public String getPlayerUuidAsString() {
        return minecraftClient.player == null ? "" : minecraftClient.player.getStringUUID();
    }

    @Override
    public String getPlayerNameString() {
        return minecraftClient.player == null ? "" : minecraftClient.player.getName().getString();
    }

    @Override
    public boolean isCurrentScreenInstanceOfHandledScreen() {
        return minecraftClient.screen instanceof InventoryScreen;
    }

    @Override
    public IScreenHandler getScreenHandler() {
        return minecraftClient.player != null ? new ScreenHandlerImpl(minecraftClient.player.containerMenu) : null;
    }

    public IInventory getInventory() {
        return minecraftClient.player != null ? new InventoryImpl(minecraftClient) : null;
    }
}
