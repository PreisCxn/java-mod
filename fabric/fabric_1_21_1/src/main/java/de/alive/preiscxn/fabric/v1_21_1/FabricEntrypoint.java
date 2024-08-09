package de.alive.preiscxn.fabric.v1_21_1;

import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.interfaces.Entrypoint;
import de.alive.preiscxn.api.interfaces.IGameHud;
import de.alive.preiscxn.api.interfaces.IInventory;
import de.alive.preiscxn.api.interfaces.IKeyBinding;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.interfaces.IPlayer;
import de.alive.preiscxn.api.interfaces.VersionedTabGui;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import de.alive.preiscxn.fabric.v1_21_1.impl.GameHudImpl;
import de.alive.preiscxn.fabric.v1_21_1.impl.InventoryImpl;
import de.alive.preiscxn.fabric.v1_21_1.impl.KeyBindingImpl;
import de.alive.preiscxn.fabric.v1_21_1.impl.MinecraftClientImpl;
import de.alive.preiscxn.fabric.v1_21_1.impl.PlayerImpl;
import de.alive.preiscxn.fabric.v1_21_1.impl.PriceTextImpl;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.NotNull;

public class FabricEntrypoint implements Entrypoint {
    @Override
    public IInventory createInventory() {
        return new InventoryImpl(MinecraftClient.getInstance());
    }

    @Override
    public IKeyBinding createKeyBinding(int code, String translationKey, String category, @NotNull KeybindExecutor keybindExecutor, boolean inInventory) {
        return new KeyBindingImpl(KeyBindingHelper.registerKeyBinding(
                new KeyBinding(translationKey, InputUtil.Type.KEYSYM, code, category)),
                keybindExecutor,
                inInventory);
    }

    @Override
    public IMinecraftClient createMinecraftClient() {
        return new MinecraftClientImpl(MinecraftClient.getInstance());
    }

    @Override
    public IGameHud createGameHub() {
        return new GameHudImpl(MinecraftClient.getInstance().inGameHud);
    }

    @Override
    public VersionedTabGui createVersionedTabGui() {
        return (VersionedTabGui) MinecraftClient.getInstance().inGameHud.getPlayerListHud();
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
