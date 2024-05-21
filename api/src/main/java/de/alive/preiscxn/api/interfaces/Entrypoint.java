package de.alive.preiscxn.api.interfaces;

import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import org.jetbrains.annotations.NotNull;

public interface Entrypoint {
    IInventory createInventory();
    IKeyBinding createKeyBinding(int code, String translationKey, String category, @NotNull KeybindExecutor keybindExecutor, boolean inInventory);
    IMinecraftClient createMinecraftClient();
    IGameHud createGameHub();
    VersionedTabGui createVersionedTabGui();
    IPlayer createPlayer();
    PriceText<?> createPriceText(boolean b);
}
