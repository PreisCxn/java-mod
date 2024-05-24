package de.alive.preiscxn.core;

import de.alive.preiscxn.api.Mod;
import de.alive.preiscxn.api.PriceCxn;
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
import de.alive.preiscxn.api.module.Module;
import de.alive.preiscxn.api.module.ModuleLoader;
import de.alive.preiscxn.api.module.PriceCxnModule;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.api.networking.Http;
import de.alive.preiscxn.api.networking.cdn.CdnFileHandler;
import de.alive.preiscxn.core.events.ConfigChangeListener;
import de.alive.preiscxn.core.events.ItemStackTooltipListener;
import de.alive.preiscxn.core.events.TickListener;
import de.alive.preiscxn.core.generated.DefaultReferenceStorage;
import de.alive.preiscxn.core.impl.LabyEntrypoint;
import de.alive.preiscxn.core.impl.LabyPlayer;
import de.alive.preiscxn.core.impl.LoggerImpl;
import de.alive.preiscxn.core.impl.PriceTextImpl;
import de.alive.preiscxn.impl.Version;
import de.alive.preiscxn.impl.cytooxien.CxnListener;
import de.alive.preiscxn.impl.cytooxien.PriceCxnItemStackImpl;
import de.alive.preiscxn.impl.keybinds.OpenBrowserKeybindExecutor;
import de.alive.preiscxn.impl.keybinds.SwitchItemViewKeybindExecutor;
import de.alive.preiscxn.impl.modules.ClasspathModule;
import de.alive.preiscxn.impl.modules.MainModule;
import de.alive.preiscxn.impl.modules.ModuleLoaderImpl;
import de.alive.preiscxn.impl.modules.RemoteModule;
import de.alive.preiscxn.impl.networking.HttpImpl;
import de.alive.preiscxn.impl.networking.cdn.CdnFileHandlerImpl;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.gui.screen.key.HotkeyService;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.models.addon.annotation.AddonMain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@AddonMain
public class PriceCxnAddon extends LabyAddon<PriceCxnConfiguration> implements Mod {
    public static final Style DEFAULT_STYLE = Style.EMPTY.color(NamedTextColor.GRAY);

    private final Map<Class<? extends KeybindExecutor>, IKeyBinding> classKeyBindingMap = new HashMap<>();
    private final Map<IKeyBinding, KeybindExecutor> keyBindingKeybindExecutorMap = new HashMap<>();
    private final ModuleLoader projectLoader;

    private final CxnListener cxnListener;
    private final CdnFileHandler cdnFileHandler;
    private final Http http;
    private final TickListener tickListener;
    private final ILogger logger = new LoggerImpl(logger());
    private PriceCxnItemStack.ViewMode viewMode = PriceCxnItemStack.ViewMode.CURRENT_STACK;

    public PriceCxnAddon() {
        this.logger().info("Creating PriceCxn client");
        this.http = new HttpImpl();
        this.cdnFileHandler = new CdnFileHandlerImpl(http);

        try {
            Field mod = PriceCxn.class
                    .getDeclaredField("mod");
            mod.setAccessible(true);
            mod.set(null, this);
            mod.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        PriceCxn.getMod().getLogger().info("PriceCxn client created");

        this.projectLoader = new ModuleLoaderImpl();

        this.projectLoader.addModule(new MainModule());
        this.tickListener = new TickListener(this::getMinecraftClient);

        try {
            cxnListener = new CxnListener();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.projectLoader.addModule(new ClasspathModule("de.alive.preiscxn.api", Thread.currentThread().getContextClassLoader()));
        this.projectLoader.addModule(new ClasspathModule("de.alive.scanner.inventory", Thread.currentThread().getContextClassLoader()));

        registerRemoteModule(
                "de.alive.inventory.listener.AuctionHouseListener",
                "Listener.jar",
                Path.of("./downloads/" + "MOD_NAME" + "_modules/cxn.listener.jar"),
                "de.alive.inventory")
                .doOnNext(module1 -> {
                    PriceCxn.getMod().getLogger().info("Adding module: {}", module1);
                    this.projectLoader.addModule(module1);
                    this.cxnListener.loadModules(this.projectLoader);

                    Set<Class<? extends PriceCxnModule>> classes1 = this.projectLoader.loadInterfaces(PriceCxnModule.class);
                    classes1.forEach(aClass -> {
                        PriceCxn.getMod().getLogger().info("Loading module: {}", aClass);
                        try {
                            aClass.getConstructor().newInstance().loadModule();
                            PriceCxn.getMod().getLogger().info("Loaded module: {}", aClass);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                                 | NoSuchMethodException e) {
                            PriceCxn.getMod().getLogger().error("Failed to load module: {}", aClass, e);
                        }
                    });
                }).subscribe();
    }

    private Mono<Module> registerRemoteModule(String classPath, String remotePath, Path localPath, String primaryPackage) {
        boolean useRemote;
        try {
            Thread.currentThread().getContextClassLoader().loadClass(classPath);
            useRemote = false;
        } catch (Exception e) {
            useRemote = true;
        }

        PriceCxn.getMod().getLogger().info("Registering remote module: {} ({}), local path: {}, primary package: {}, use remote: {}",
                classPath, remotePath, localPath, primaryPackage, useRemote);
        return RemoteModule.create(remotePath,
                localPath,
                primaryPackage,
                useRemote,
                Thread.currentThread().getContextClassLoader());
    }

    @Override
    protected void enable() {
        this.registerSettingCategory();

        this.registerListener(tickListener);
        this.registerListener(new ItemStackTooltipListener());
        this.registerListener(new ConfigChangeListener());
        PriceCxnConfiguration priceCxnConfiguration = this.configuration();

        registerKeybinding(
                priceCxnConfiguration.getOpenInBrowser(),
                new OpenBrowserKeybindExecutor(),
                true);
        registerKeybinding(
                priceCxnConfiguration.getCycleAmount(),
                new SwitchItemViewKeybindExecutor(),
                true);

        AtomicBoolean currentCoinSetting = new AtomicBoolean(getConfig().isDisplayCoin());
        tickListener.addTickConsumer(iMinecraftClient -> {
            if(getConfig().isDisplayCoin() != currentCoinSetting.get()) {
                currentCoinSetting.set(getConfig().isDisplayCoin());
            }
        });
    }

    @Override
    protected Class<PriceCxnConfiguration> configurationClass() {
        return PriceCxnConfiguration.class;
    }

    @Override
    public String getVersion() {
        return Version.MOD_VERSION;
    }

    @Override
    public Style getDefaultStyle() {
        return DEFAULT_STYLE;
    }

    @Override
    public Component getModText() {
        return Component.text("[")
                .color(NamedTextColor.DARK_GRAY)
                .append(Component.translatable("cxn_listener.mod_text")
                        .color(NamedTextColor.GOLD))
                .append(Component.text("] ")
                        .color(NamedTextColor.DARK_GRAY));
    }

    @Override
    public void printTester(String message) {
        PriceCxn.getMod().getLogger().debug("[PCXN-TESTER] : {}", message);
        if (!DEBUG_MODE && !TESTER_MODE) return;

        if (labyAPI().minecraft() == null) return;

        getMinecraftClient().sendMessage(message);
    }

    @Override
    public void printDebug(String message, boolean overlay, boolean sysOut) {
        PriceCxn.getMod().getLogger().debug("[PCXN-DEBUG] : {}", message);
        if (!DEBUG_MODE) return;
        if (labyAPI().minecraft() == null) return;
        if (labyAPI().minecraft().getClientPlayer() == null) return;

        getMinecraftClient().sendMessage(message);
    }

    @Override
    public void printDebug(String message, boolean overlay) {
        printDebug(message, overlay, false);
    }

    @Override
    public void printDebug(String message) {
        printDebug(message, true, false);
    }

    @Override
    public PriceText<?> createPriceText() {
        return new PriceTextImpl(false);
    }

    @Override
    public PriceText<?> createPriceText(boolean b) {
        return new PriceTextImpl(b);
    }

    @Override
    public Object space() {
        return Component.text(" ");
    }

    @Override
    public PriceCxnConfig getConfig() {
        return configuration();
    }

    @Override
    public PriceCxnItemStack createItemStack(@NotNull IItemStack item,
                                             @Nullable Map<String, DataAccess> searchData,
                                             boolean addComment,
                                             boolean addTooltips) {
        return PriceCxnItemStackImpl.getInstance(item, searchData, addComment, addTooltips);
    }

    @Override
    public PriceCxnItemStack createItemStack(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment) {
        return PriceCxnItemStackImpl.getInstance(item, searchData, addComment);
    }

    @Override
    public PriceCxnItemStack createItemStack(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData) {
        return PriceCxnItemStackImpl.getInstance(item, searchData);
    }

    @Override
    public IInventory createInventory() {
        return getLabyEntrypoint().createInventory();
    }

    @Override
    public ICxnListener getCxnListener() {
        return cxnListener;
    }

    @Override
    public IPlayer getPlayer() {
        return new LabyPlayer(this.labyAPI().minecraft());
    }

    @Override
    public void runOnEndClientTick(Consumer<IMinecraftClient> consumer) {
        this.tickListener.addTickConsumer(consumer);
    }

    @Override
    public void runOnJoin(Consumer<IMinecraftClient> consumer) {
        this.tickListener.addJoinConsumer(consumer);
    }

    @Override
    public void runOnDisconnect(Consumer<IMinecraftClient> consumer) {
        this.tickListener.addDisconnectConsumer(consumer);
    }

    @Override
    public CdnFileHandler getCdnFileHandler() {
        return cdnFileHandler;
    }

    @Override
    public IMinecraftClient getMinecraftClient() {
        return getLabyEntrypoint().createMinecraftClient();
    }

    @Override
    public Http getHttp() {
        return http;
    }

    private void registerKeybinding(ConfigProperty<Key> keyConfigProperty, @NotNull KeybindExecutor keybindExecutor, boolean inInventory) {
        IKeyBinding keyBinding = getLabyEntrypoint()
                .createKeyBinding(keyConfigProperty.get().getId(), keyConfigProperty.get().getTranslationKey(), keyConfigProperty.get().getName(), keybindExecutor, inInventory);

        Laby.references().hotkeyService().register(keyConfigProperty.get().getName(), keyConfigProperty, () -> HotkeyService.Type.TOGGLE, active -> {
            if (active) {
                keybindExecutor.onKeybindPressed(
                        getLabyEntrypoint().createMinecraftClient(),
                        getLabyEntrypoint().createInventory().getMainHandStack()
                );
            }
        });

        classKeyBindingMap.put(keybindExecutor.getClass(), keyBinding);
        if (keyBinding.isInInventory())
            keyBindingKeybindExecutorMap.put(keyBinding, keybindExecutor);
    }

    @Override
    public IKeyBinding getKeyBinding(Class<? extends KeybindExecutor> keybindExecutorClass) {
        return classKeyBindingMap.get(keybindExecutorClass);
    }

    @Override
    public void forEachKeybindExecutor(BiConsumer<? super IKeyBinding, ? super KeybindExecutor> keyBinding) {
        keyBindingKeybindExecutorMap.forEach(keyBinding);
    }

    @Override
    public ModuleLoader getProjectLoader() {
        return this.projectLoader;
    }

    @Override
    public PriceCxnItemStack.ViewMode getViewMode() {
        return this.viewMode;
    }

    @Override
    public void nextViewMode() {
        this.viewMode = PriceCxnItemStack.ViewMode.values()[(this.viewMode.ordinal() + 1) % PriceCxnItemStack.ViewMode.values().length];
    }

    @Override
    public ICxnConnectionManager getConnectionManager() {
        return cxnListener.getConnectionManager();
    }

    @Override
    public ILogger getLogger() {
        return this.logger;
    }

    @Override
    public IGameHud getGameHud() {
        return getLabyEntrypoint().createGameHub();
    }

    @Override
    public VersionedTabGui getVersionedTabGui() {
        return getLabyEntrypoint().createVersionedTabGui();
    }

    @Override
    public void openUrl(String url) {
        getMinecraftClient().openUrl(url);
    }

    private LabyEntrypoint getLabyEntrypoint() {
        return ((DefaultReferenceStorage) this.referenceStorageAccessor()).labyEntrypoint();
    }
}
