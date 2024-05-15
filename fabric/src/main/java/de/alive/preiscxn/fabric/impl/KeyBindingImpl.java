package de.alive.preiscxn.fabric.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.alive.preiscxn.api.interfaces.IKeyBinding;
import net.minecraft.client.option.KeyBinding;

import java.util.concurrent.ExecutionException;

public class KeyBindingImpl implements IKeyBinding {
    private static final Cache<KeyBinding, KeyBindingImpl> CACHE = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .build();
    private final KeyBinding keyBinding;

    public KeyBindingImpl(KeyBinding keyBinding) {
        this.keyBinding = keyBinding;
    }

    public static KeyBindingImpl getInstance(KeyBinding keyBinding) {
        try {
            return CACHE.get(keyBinding, () -> new KeyBindingImpl(keyBinding));
        } catch (ExecutionException e) {
            return new KeyBindingImpl(keyBinding);
        }
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
}
