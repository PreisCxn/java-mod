package de.alive.preiscxn.api;

import de.alive.preiscxn.api.cytooxien.ICxnConnectionManager;
import de.alive.preiscxn.api.cytooxien.ICxnListener;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.interfaces.IKeyBinding;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.interfaces.IPlayer;
import de.alive.preiscxn.api.keybinds.CustomKeyBinding;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import de.alive.preiscxn.api.module.ModuleLoader;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.api.networking.Http;
import de.alive.preiscxn.api.networking.cdn.CdnFileHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Mod {
    String getVersion();
    Object getDefaultStyle();
    Object getModText();
    PriceCxnItemStack createItemStack(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips);
    PriceCxnItemStack createItemStack(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment);
    PriceCxnItemStack createItemStack(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData);

    ICxnListener getCxnListener();
    IPlayer getPlayer();
    void runOnEndClientTick(Consumer<IMinecraftClient> consumer);
    CdnFileHandler getCdnFileHandler();
    IMinecraftClient getMinecraftClient();
    Http getHttp();
    void registerKeybinding(@NotNull CustomKeyBinding keyBinding, @NotNull KeybindExecutor keybindExecutor, boolean inInventory);
    IKeyBinding getKeyBinding(Class<? extends KeybindExecutor> keybindExecutorClass);
    void forEachKeybindExecutor(BiConsumer<? super IKeyBinding, ? super KeybindExecutor> keyBinding);
    ModuleLoader getProjectLoader();
    PriceCxnItemStack.ViewMode getViewMode();
    void nextViewMode();
    ICxnConnectionManager getConnectionManager();
}
