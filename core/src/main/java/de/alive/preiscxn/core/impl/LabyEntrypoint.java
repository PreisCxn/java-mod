package de.alive.preiscxn.core.impl;

import de.alive.preiscxn.api.interfaces.Entrypoint;
import de.alive.preiscxn.api.interfaces.VersionedTabGui;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import net.labymod.api.reference.annotation.Referenceable;
import org.jetbrains.annotations.NotNull;

@Referenceable
public interface LabyEntrypoint extends Entrypoint {
    LabyInventory createInventory();
    LabyKeyBinding createKeyBinding(int code, String translationKey, String category, @NotNull KeybindExecutor keybindExecutor, boolean inInventory);
    LabyMinecraftClient createMinecraftClient();
    LabyGameHub createGameHub();
    VersionedTabGui createVersionedTabGui();
}
