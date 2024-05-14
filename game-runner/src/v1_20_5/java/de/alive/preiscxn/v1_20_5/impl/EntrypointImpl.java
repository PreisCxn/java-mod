package de.alive.preiscxn.v1_20_5.impl;

import de.alive.api.keybinds.CustomKeyBinding;
import de.alive.preiscxn.core.impl.LabyEntrypoint;
import de.alive.preiscxn.core.impl.LabyInventory;
import de.alive.preiscxn.core.impl.LabyKeyBinding;
import de.alive.preiscxn.core.impl.LabyMinecraftClient;
import de.alive.preiscxn.core.impl.LabyScreenHandler;
import net.labymod.api.models.Implements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import javax.inject.Singleton;

@Singleton
@Implements(LabyEntrypoint.class)
public class EntrypointImpl implements LabyEntrypoint {
    @Override
    public LabyInventory createInventory() {
        return new InventoryImpl().setMinecraftClient(Minecraft.getInstance());
    }

    @Override
    public LabyKeyBinding createKeyBinding(CustomKeyBinding customKeyBinding) {
        return new KeyBindingImpl().setKeyBinding(new KeyMapping(customKeyBinding.getTranslationKey(),
                customKeyBinding.getCode(),
                customKeyBinding.getCategory()));
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
}
