package de.alive.pricecxn.impl;

import de.alive.pricecxn.interfaces.IMinecraftClient;
import de.alive.pricecxn.interfaces.IScreenHandler;
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
    public boolean isCurrentScreenTitleNull() {
        return minecraftClient.currentScreen == null || minecraftClient.currentScreen.getTitle() == null;
    }

    @Override
    public String getCurrentScreenTitle() {
        return minecraftClient.currentScreen == null ? "" : minecraftClient.currentScreen.getTitle().getString();
    }

    @Override
    public boolean containsInTitle(String s) {
        return minecraftClient.currentScreen == null || minecraftClient.currentScreen.getTitle().getString().contains(s);
    }

    @Override
    public boolean equalsTitle(String s) {
        return minecraftClient.currentScreen == null || minecraftClient.currentScreen.getTitle().getString().equals(s);
    }

    @Override
    public int getInventorySize() {
        return minecraftClient.player == null ? 0 : minecraftClient.player.currentScreenHandler.getSlot(0).inventory.size();
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
}
