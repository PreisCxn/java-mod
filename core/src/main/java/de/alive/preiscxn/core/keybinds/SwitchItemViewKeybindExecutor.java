package de.alive.preiscxn.core.keybinds;

import de.alive.api.PriceCxn;
import de.alive.api.interfaces.IItemStack;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.keybinds.KeybindExecutor;

public class SwitchItemViewKeybindExecutor implements KeybindExecutor {
    @Override
    public void onKeybindPressed(IMinecraftClient client, IItemStack itemStack) {
        PriceCxn.getMod().nextViewMode();
    }
}
