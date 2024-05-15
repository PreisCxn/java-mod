package de.alive.preiscxn.core.impl;

import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import net.labymod.api.reference.annotation.Referenceable;
import org.jetbrains.annotations.NotNull;

@Referenceable
public interface LabyEntrypoint {
    LabyInventory createInventory();
    LabyKeyBinding createKeyBinding(int code, String translationKey, String category, @NotNull KeybindExecutor keybindExecutor, boolean inInventory);
    LabyMinecraftClient createMinecraftClient();
    LabyScreenHandler createScreenHandler();
    LabyGameHub createGameHub();
}
