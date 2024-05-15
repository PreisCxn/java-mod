package de.alive.preiscxn.api.interfaces;

import de.alive.preiscxn.api.keybinds.KeybindExecutor;

public interface IKeyBinding {
    boolean wasPressed();

    boolean isUnbound();

    String getBoundKeyLocalizedText();

    boolean matchesKey(int keyCode, int scanCode);

    KeybindExecutor getKeybindExecutor();

    boolean isInInventory();
}
