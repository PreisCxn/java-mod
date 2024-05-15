package de.alive.preiscxn.core.impl;

import de.alive.preiscxn.api.keybinds.CustomKeyBinding;
import net.labymod.api.reference.annotation.Referenceable;

@Referenceable
public interface LabyEntrypoint {
    LabyInventory createInventory();
    LabyKeyBinding createKeyBinding(CustomKeyBinding customKeyBinding);
    LabyMinecraftClient createMinecraftClient();
    LabyScreenHandler createScreenHandler();
}
