package de.alive.preiscxn.v1_20_6.impl;

import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.interfaces.IPlayer;
import de.alive.preiscxn.api.interfaces.VersionedTabGui;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import de.alive.preiscxn.core.impl.LabyEntrypoint;
import de.alive.preiscxn.core.impl.LabyGameHub;
import de.alive.preiscxn.core.impl.LabyInventory;
import de.alive.preiscxn.core.impl.LabyKeyBinding;
import de.alive.preiscxn.core.impl.LabyMinecraftClient;
import de.alive.preiscxn.fabric.v1_21.impl.PlayerImpl;
import de.alive.preiscxn.fabric.v1_21.impl.PriceTextImpl;
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
    public LabyGameHub createGameHub() {
        return new GameHubImpl();
    }

    @Override
    public VersionedTabGui createVersionedTabGui() {
        return (VersionedTabGui) Minecraft.getInstance().gui.getTabList();
    }

    @Override
    public IPlayer createPlayer() {
        return new PlayerImpl();
    }

    @Override
    public PriceText<?> createPriceText(boolean b) {
        return new PriceTextImpl(b);
    }
}
