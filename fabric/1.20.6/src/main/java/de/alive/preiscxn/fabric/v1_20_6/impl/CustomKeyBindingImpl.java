package de.alive.preiscxn.fabric.v1_20_6.impl;

import de.alive.preiscxn.api.keybinds.CustomKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@SuppressWarnings("unused")
public record CustomKeyBindingImpl(String translationKey, int code, String category) implements CustomKeyBinding {

    public KeyBinding getKeybinding() {
        return new KeyBinding(translationKey, InputUtil.Type.KEYSYM, code, category);
    }

    public KeyBinding registerKeybinding() {
        return KeyBindingHelper.registerKeyBinding(getKeybinding());
    }

}
