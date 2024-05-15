package de.alive.preiscxn.impl.keybinds;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;

public class SwitchItemViewKeybindExecutor implements KeybindExecutor {
    @Override
    public void onKeybindPressed(IMinecraftClient client, IItemStack itemStack) {
        PriceCxn.getMod().nextViewMode();
    }
}
