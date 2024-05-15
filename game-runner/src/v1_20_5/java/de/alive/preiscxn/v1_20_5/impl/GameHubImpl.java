package de.alive.preiscxn.v1_20_5.impl;

import de.alive.preiscxn.core.impl.LabyGameHub;
import net.labymod.api.models.Implements;
import net.minecraft.client.gui.Gui;

import java.lang.reflect.Field;

@Implements(LabyGameHub.class)
public class GameHubImpl implements LabyGameHub {
    private Gui gameHud;

    public GameHubImpl setGameHud(Gui gameHud) {
        this.gameHud = gameHud;
        return this;
    }

    @Override
    public boolean isGameHudNull() {
        return gameHud == null;
    }

    @Override
    public boolean isPlayerListHudNull() {
        return gameHud == null;
    }

    @Override
    public Field[] getDeclaredPlayerListFields() {
        return gameHud.getTabList().getClass().getDeclaredFields();
    }

    @Override
    public Object getGameHud() {
        return gameHud.getTabList();
    }
}
