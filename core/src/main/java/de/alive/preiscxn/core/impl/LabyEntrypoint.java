package de.alive.preiscxn.core.impl;

import de.alive.api.keybinds.CustomKeyBinding;
import net.labymod.api.reference.annotation.Referenceable;

@Referenceable
public interface LabyEntrypoint {
    LabyInventory createInventory();
    LabyKeyBinding createKeyBinding(CustomKeyBinding customKeyBinding);
    LabyMinecraftClient createMinecraftClient();
    LabyScreenHandler createScreenHandler();
}
