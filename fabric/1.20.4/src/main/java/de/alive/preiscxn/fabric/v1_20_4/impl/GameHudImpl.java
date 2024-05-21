package de.alive.preiscxn.fabric.v1_20_4.impl;

import de.alive.preiscxn.api.interfaces.IGameHud;
import net.minecraft.client.gui.hud.InGameHud;

import java.lang.reflect.Field;

public class GameHudImpl implements IGameHud {
    private final InGameHud gameHud;

    public GameHudImpl(InGameHud gameHud) {
        this.gameHud = gameHud;
    }

    @Override
    public boolean isGameHudNull() {
        return gameHud == null;
    }

    @Override
    public boolean isPlayerListHudNull() {
        return gameHud.getPlayerListHud() == null;
    }

    @Override
    public Field[] getDeclaredPlayerListFields() {
        return gameHud.getPlayerListHud().getClass().getDeclaredFields();
    }

    @Override
    public Object getGameHud() {
        return gameHud.getPlayerListHud();
    }
}
