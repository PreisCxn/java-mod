package de.alive.preiscxn.v1_20_5.impl;

import de.alive.api.interfaces.IInventory;
import de.alive.api.interfaces.IScreenHandler;
import de.alive.preiscxn.core.impl.LabyMinecraftClient;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

@Implements(LabyMinecraftClient.class)
public final class MinecraftClientImpl implements LabyMinecraftClient {
    private Minecraft minecraftClient;

    public MinecraftClientImpl() {

    }

    MinecraftClientImpl setMinecraftClient(Minecraft minecraftClient) {
        this.minecraftClient = minecraftClient;
        return this;
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
        return minecraftClient.player != null ? new ScreenHandlerImpl().setScreenHandler(minecraftClient.player.containerMenu) : null;
    }

    public IInventory getInventory() {
        return minecraftClient.player != null ? new InventoryImpl().setMinecraftClient(minecraftClient) : null;
    }
}
