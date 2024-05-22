package de.alive.preiscxn.v1_20_6.impl;

import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import de.alive.preiscxn.core.impl.LabyKeyBinding;
import net.labymod.api.models.Implements;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;

@Implements(LabyKeyBinding.class)
public class KeyBindingImpl implements LabyKeyBinding {
    private KeyMapping keyBinding;
    private KeybindExecutor keybindExecutor;
    private boolean inInventory;

    public KeyBindingImpl() {
    }

    KeyBindingImpl setKeyBinding(KeyMapping keyBinding, @NotNull KeybindExecutor keybindExecutor, boolean inInventory) {
        this.keyBinding = keyBinding;
        this.keybindExecutor = keybindExecutor;
        this.inInventory = inInventory;
        return this;
    }

    @Override
    public boolean wasPressed() {
        return keyBinding.isDown();
    }

    @Override
    public boolean isUnbound() {
        return keyBinding.isUnbound();
    }

    @Override
    public String getBoundKeyLocalizedText() {
        return keyBinding.getTranslatedKeyMessage().getString();
    }

    @Override
    public boolean matchesKey(int keyCode, int scanCode) {
        return keyBinding.matches(keyCode, scanCode);
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
