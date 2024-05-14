package de.alive.preiscxn.v1_20_5.impl;

import net.minecraft.client.Minecraft;
import de.alive.preiscxn.core.impl.LabyInventory;
import net.labymod.api.models.Implements;

@Implements(LabyInventory.class)
public final class InventoryImpl implements LabyInventory {
    private final Minecraft minecraftClient;

    public InventoryImpl(Minecraft minecraftClient) {
        this.minecraftClient = minecraftClient;
    }

    @Override
    public String getTitle() {
        return minecraftClient.screen == null ? null : minecraftClient.screen.getTitle().getString();
    }

    @Override
    public int getSize() {
        if (minecraftClient.player == null) {
            return 0;
        } else {
            minecraftClient.player.containerMenu.getSlot(0);
        }

        return minecraftClient.player == null ? 0 : minecraftClient.player.containerMenu.getSlot(0).container.getContainerSize();
    }
}
