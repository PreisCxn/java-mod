package de.alive.preiscxn.v1_20_5.impl;

import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.core.impl.LabyInventory;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;

@Implements(LabyInventory.class)
public final class InventoryImpl implements LabyInventory {
    private Minecraft minecraftClient;

    public InventoryImpl() {
    }

    InventoryImpl setMinecraftClient(Minecraft minecraftClient) {
        this.minecraftClient = minecraftClient;
        return this;
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

    @Override
    public IItemStack getMainHandStack() {
        if(minecraftClient.player == null)
            return null;
        return (IItemStack) (Object) minecraftClient.player.getMainHandItem();
    }
}
