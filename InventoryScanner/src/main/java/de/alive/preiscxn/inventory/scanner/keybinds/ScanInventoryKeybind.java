package de.alive.preiscxn.inventory.scanner.keybinds;

import de.alive.api.interfaces.IItemStack;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.keybinds.KeybindExecutor;

public class ScanInventoryKeybind implements KeybindExecutor {
    @Override
    public void onKeybindPressed(IMinecraftClient client, IItemStack itemStack) {
        client.getInventory();
    }
}
