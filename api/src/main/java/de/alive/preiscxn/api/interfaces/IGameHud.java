package de.alive.preiscxn.api.interfaces;

import java.lang.reflect.Field;

public interface IGameHud {
    boolean isGameHudNull();

    boolean isPlayerListHudNull();

    Field[] getDeclaredPlayerListFields();

    Object getGameHud();
}
