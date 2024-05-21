package de.alive.preiscxn.fabric.v1_20_6.impl;

import de.alive.preiscxn.api.interfaces.IPlayer;
import net.minecraft.client.MinecraftClient;

public class PlayerImpl implements IPlayer {
    @Override
    public String getName() {
        if (MinecraftClient.getInstance().player != null) {
            return MinecraftClient.getInstance().player.getName().getString();
        }
        return "";
    }

    @Override
    public String getUUIDasString() {
        if (MinecraftClient.getInstance().player != null) {
            return MinecraftClient.getInstance().player.getUuidAsString();
        }
        return "";
    }
}
