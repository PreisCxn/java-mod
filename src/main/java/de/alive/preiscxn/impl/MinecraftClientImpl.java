package de.alive.preiscxn.impl;

import de.alive.api.interfaces.IInventory;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.interfaces.IScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class MinecraftClientImpl implements IMinecraftClient {
    private final MinecraftClient minecraftClient;

    public MinecraftClientImpl(MinecraftClient minecraftClient) {
        this.minecraftClient = minecraftClient;
    }

    @Override
    public boolean isPlayerNull() {
        return minecraftClient.player == null;
    }

    @Override
    public boolean isCurrentScreenNull() {
        return minecraftClient.currentScreen == null;
    }

    @Override
    public boolean isCurrentScreenHandlerNull() {
        return minecraftClient.player == null || minecraftClient.player.currentScreenHandler == null;
    }

    @Override
    public String getPlayerUuidAsString() {
        return minecraftClient.player == null ? "" : minecraftClient.player.getUuidAsString();
    }

    @Override
    public String getPlayerNameString() {
        return minecraftClient.player == null ? "" : minecraftClient.player.getName().getString();
    }

    @Override
    public boolean isCurrentScreenInstanceOfHandledScreen() {
        return minecraftClient.currentScreen instanceof HandledScreen;
    }

    @Override
    public IScreenHandler getScreenHandler() {
        return minecraftClient.player != null ? new ScreenHandlerImpl(minecraftClient.player.currentScreenHandler) : null;
    }

    public IInventory getInventory(){
        return minecraftClient.player != null ? new InventoryImpl(minecraftClient, minecraftClient.player.getInventory()) : null;
    }
}
