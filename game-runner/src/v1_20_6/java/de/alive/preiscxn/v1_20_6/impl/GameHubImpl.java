package de.alive.preiscxn.v1_20_6.impl;

import de.alive.preiscxn.core.impl.LabyGameHub;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;

@Implements(LabyGameHub.class)
public class GameHubImpl implements LabyGameHub {

    @Override
    public boolean isGameHudNull() {
        return false;
    }

    @Override
    public boolean isPlayerListHudNull() {
        return false;
    }

    @Override
    public Field[] getDeclaredPlayerListFields() {
        return Minecraft.getInstance().gui.getTabList().getClass().getDeclaredFields();
    }

    @Override
    public Object getGameHud() {
        return Minecraft.getInstance().gui.getTabList();
    }
}
