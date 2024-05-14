package de.alive.preiscxn.fabric.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.alive.api.interfaces.IInventory;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.interfaces.IScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

import java.util.concurrent.ExecutionException;

public final class MinecraftClientImpl implements IMinecraftClient {
    private static final Cache<MinecraftClient, MinecraftClientImpl> CACHE = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .build();
    private final MinecraftClient minecraftClient;

    private MinecraftClientImpl(MinecraftClient minecraftClient) {
        this.minecraftClient = minecraftClient;
    }

    public static MinecraftClientImpl getInstance(MinecraftClient minecraftClient) {
        try {
            return CACHE.get(minecraftClient, () -> new MinecraftClientImpl(minecraftClient));
        } catch (ExecutionException e) {
            return new MinecraftClientImpl(minecraftClient);
        }
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
        return minecraftClient.player != null ? ScreenHandlerImpl.getInstance(minecraftClient.player.currentScreenHandler) : null;
    }

    public IInventory getInventory() {
        return minecraftClient.player != null ? InventoryImpl.getInstance(minecraftClient) : null;
    }
}
