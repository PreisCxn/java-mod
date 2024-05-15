package de.alive.preiscxn.api.interfaces;

public interface IKeyBinding {
    boolean wasPressed();

    boolean isUnbound();

    String getBoundKeyLocalizedText();

    boolean matchesKey(int keyCode, int scanCode);
}
