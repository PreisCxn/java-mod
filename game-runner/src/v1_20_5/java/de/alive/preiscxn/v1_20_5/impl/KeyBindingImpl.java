package de.alive.preiscxn.v1_20_5.impl;

import de.alive.preiscxn.core.impl.LabyKeyBinding;
import net.labymod.api.models.Implements;
import net.minecraft.client.KeyMapping;

@Implements(LabyKeyBinding.class)
public class KeyBindingImpl implements LabyKeyBinding {
    private KeyMapping keyBinding;

    public KeyBindingImpl() {
    }

    KeyBindingImpl setKeyBinding(KeyMapping keyBinding) {
        this.keyBinding = keyBinding;
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
}
