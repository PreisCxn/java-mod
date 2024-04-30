package de.alive.api;

import de.alive.api.cytooxien.ICxnListener;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.interfaces.IPlayer;
import de.alive.api.keybinds.CustomKeyBinding;
import de.alive.api.keybinds.KeybindExecutor;
import de.alive.api.module.ModuleLoader;
import de.alive.api.networking.DataAccess;
import de.alive.api.networking.Http;
import de.alive.api.networking.cdn.CdnFileHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Mod {
    PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips);
    PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment);
    PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData);

    ICxnListener getCxnListener();
    IPlayer getPlayer();
    void runOnEndClientTick(Consumer<IMinecraftClient> consumer);
    CdnFileHandler getCdnFileHandler();
    IMinecraftClient getMinecraftClient();
    Http getHttp();
    void registerKeybinding(@NotNull CustomKeyBinding keyBinding, @NotNull KeybindExecutor keybindExecutor, boolean inInventory);
    KeyBinding getKeyBinding(Class<? extends KeybindExecutor> keybindExecutorClass);
    void forEachKeybindExecutor(BiConsumer<? super KeyBinding, ? super KeybindExecutor> keyBinding);
    ModuleLoader getProjectLoader();
    PriceCxnItemStack.ViewMode getViewMode();
    void nextViewMode();
}
