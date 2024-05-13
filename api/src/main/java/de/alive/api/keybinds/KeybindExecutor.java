package de.alive.api.keybinds;

import de.alive.api.interfaces.IItemStack;
import de.alive.api.interfaces.IMinecraftClient;

public interface KeybindExecutor {
    void onKeybindPressed(IMinecraftClient client, IItemStack itemStack);

}
