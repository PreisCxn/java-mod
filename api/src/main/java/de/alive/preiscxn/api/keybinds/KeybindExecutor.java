package de.alive.preiscxn.api.keybinds;

import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;

public interface KeybindExecutor {
    void onKeybindPressed(IMinecraftClient client, IItemStack itemStack);

}
