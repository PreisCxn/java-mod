package de.alive.preiscxn.api;

import de.alive.preiscxn.api.cytooxien.ICxnConnectionManager;
import de.alive.preiscxn.api.cytooxien.ICxnListener;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.interfaces.IGameHud;
import de.alive.preiscxn.api.interfaces.IInventory;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.interfaces.IKeyBinding;
import de.alive.preiscxn.api.interfaces.ILogger;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.interfaces.IPlayer;
import de.alive.preiscxn.api.interfaces.PriceCxnConfig;
import de.alive.preiscxn.api.interfaces.VersionedTabGui;
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
    boolean DEBUG_MODE = System.getenv("PCXN_DEBUG_MODE") != null && System.getenv("PCXN_DEBUG_MODE").equals("true");
    boolean TESTER_MODE = System.getenv("PCXN_TESTER_MODE") != null && System.getenv("PCXN_TESTER_MODE").equals("true");

    String getVersion();
    Object getDefaultStyle();
    Object getModText();

    void printTester(String message);
    void printDebug(String message, boolean overlay, boolean sysOut);
    void printDebug(String message, boolean overlay);
    void printDebug(String message);

    PriceText<?> createPriceText();
    PriceText<?> createPriceText(boolean b);
    Object space();

    PriceCxnConfig getConfig();

    PriceCxnItemStack createItemStack(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips);
    PriceCxnItemStack createItemStack(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment);
    PriceCxnItemStack createItemStack(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData);

    IInventory createInventory();

    void runOnEndClientTick(Consumer<IMinecraftClient> consumer);
    void runOnJoin(Consumer<IMinecraftClient> consumer);
    void runOnDisconnect(Consumer<IMinecraftClient> consumer);

    ICxnListener getCxnListener();
    IPlayer getPlayer();
    CdnFileHandler getCdnFileHandler();
    IMinecraftClient getMinecraftClient();
    Http getHttp();
    IKeyBinding getKeyBinding(Class<? extends KeybindExecutor> keybindExecutorClass);
    void forEachKeybindExecutor(BiConsumer<? super IKeyBinding, ? super KeybindExecutor> keyBinding);
    ModuleLoader getProjectLoader();
    PriceCxnItemStack.ViewMode getViewMode();
    void nextViewMode();
    ICxnConnectionManager getConnectionManager();
    ILogger getLogger();

    IGameHud getGameHud();
    VersionedTabGui getVersionedTabGui();

    void openUrl(String url);
}
