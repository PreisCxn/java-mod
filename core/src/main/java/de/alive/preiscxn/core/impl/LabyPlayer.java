package de.alive.preiscxn.core.impl;

import de.alive.preiscxn.api.interfaces.IPlayer;
import net.labymod.api.client.Minecraft;
import net.labymod.api.client.entity.player.ClientPlayer;

public class LabyPlayer implements IPlayer {
    private final Minecraft minecraftClient;

    public LabyPlayer(Minecraft minecraftClient) {
        this.minecraftClient = minecraftClient;
    }
    @Override
    public String getName() {
        ClientPlayer clientPlayer = minecraftClient.getClientPlayer();
        if (clientPlayer == null) {
            return "";
        }
        return clientPlayer.getName();
    }
}
