package de.alive.api.keybinds;

import net.minecraft.item.ItemStack;

public interface KeybindExecutor {
    void onKeybindPressed(ItemStack itemStack);

}
