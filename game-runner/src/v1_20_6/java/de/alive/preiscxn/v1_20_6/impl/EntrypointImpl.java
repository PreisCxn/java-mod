package de.alive.preiscxn.v1_20_6.impl;

import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import de.alive.preiscxn.core.impl.LabyEntrypoint;
import de.alive.preiscxn.core.impl.LabyGameHub;
import de.alive.preiscxn.core.impl.LabyInventory;
import de.alive.preiscxn.core.impl.LabyKeyBinding;
import de.alive.preiscxn.core.impl.LabyMinecraftClient;
import de.alive.preiscxn.core.impl.LabyScreenHandler;
import net.labymod.api.models.Implements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

@Singleton
@Implements(LabyEntrypoint.class)
public class EntrypointImpl implements LabyEntrypoint {
    @Override
    public LabyInventory createInventory() {
        return new InventoryImpl().setMinecraftClient(Minecraft.getInstance());
    }

    @Override
    public LabyKeyBinding createKeyBinding(int code, String translationKey, String category, @NotNull KeybindExecutor keybindExecutor, boolean inInventory) {
        return new KeyBindingImpl().setKeyBinding(new KeyMapping(translationKey,
                code,
                category),
                keybindExecutor,
                inInventory);
    }

    @Override
    public LabyMinecraftClient createMinecraftClient() {
        return new MinecraftClientImpl().setMinecraftClient(Minecraft.getInstance());
    }

    @Override
    public LabyScreenHandler createScreenHandler() {
        if (Minecraft.getInstance().player == null)
            return null;

        return new ScreenHandlerImpl().setScreenHandler(Minecraft.getInstance().player.containerMenu);
    }

    @Override
    public LabyGameHub createGameHub() {
        return new GameHubImpl().setGameHud(Minecraft.getInstance().gui);
    }
}
