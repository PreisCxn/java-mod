package de.alive.preiscxn.fabric.v1_21_1.impl;

import de.alive.preiscxn.api.interfaces.IKeyBinding;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import net.minecraft.client.option.KeyBinding;

public class KeyBindingImpl implements IKeyBinding {
    private final KeyBinding keyBinding;
    private final KeybindExecutor keybindExecutor;
    private final boolean inInventory;

    public KeyBindingImpl(KeyBinding keyBinding, KeybindExecutor keybindExecutor, boolean inInventory) {
        this.keyBinding = keyBinding;
        this.keybindExecutor = keybindExecutor;
        this.inInventory = inInventory;
    }

    @Override
    public boolean wasPressed() {
        return keyBinding.wasPressed();
    }

    @Override
    public boolean isUnbound() {
        return keyBinding.isUnbound();
    }

    @Override
    public String getBoundKeyLocalizedText() {
        return keyBinding.getBoundKeyLocalizedText().getString();
    }

    @Override
    public boolean matchesKey(int keyCode, int scanCode) {
        return keyBinding.matchesKey(keyCode, scanCode);
    }

    @Override
    public KeybindExecutor getKeybindExecutor() {
        return keybindExecutor;
    }

    @Override
    public boolean isInInventory() {
        return inInventory;
    }
}
