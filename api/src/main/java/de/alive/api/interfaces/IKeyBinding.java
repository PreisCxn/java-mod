package de.alive.api.interfaces;

public interface IKeyBinding {
    boolean wasPressed();

    boolean isUnbound();

    String getBoundKeyLocalizedText();

    boolean matchesKey(int keyCode, int scanCode);
}
