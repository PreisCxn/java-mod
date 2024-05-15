package de.alive.preiscxn.core;

import de.alive.preiscxn.api.Mod;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.ICxnConnectionManager;
import de.alive.preiscxn.api.cytooxien.ICxnListener;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.interfaces.IGameHud;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.interfaces.IKeyBinding;
import de.alive.preiscxn.api.interfaces.ILogger;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.interfaces.IPlayer;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import de.alive.preiscxn.api.module.Module;
import de.alive.preiscxn.api.module.ModuleLoader;
import de.alive.preiscxn.api.module.PriceCxnModule;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.api.networking.Http;
import de.alive.preiscxn.api.networking.cdn.CdnFileHandler;
import de.alive.preiscxn.core.events.TickListener;
import de.alive.preiscxn.core.generated.DefaultReferenceStorage;
import de.alive.preiscxn.core.impl.LabyEntrypoint;
import de.alive.preiscxn.core.impl.LabyPlayer;
import de.alive.preiscxn.core.impl.LoggerImpl;
import de.alive.preiscxn.core.impl.PriceTextImpl;
import de.alive.preiscxn.impl.Version;
import de.alive.preiscxn.impl.cytooxien.CxnListener;
import de.alive.preiscxn.impl.cytooxien.PriceCxnItemStackImpl;
import de.alive.preiscxn.impl.modules.ClasspathModule;
import de.alive.preiscxn.impl.modules.MainModule;
import de.alive.preiscxn.impl.modules.ModuleLoaderImpl;
import de.alive.preiscxn.impl.modules.RemoteModule;
import de.alive.preiscxn.impl.networking.HttpImpl;
import de.alive.preiscxn.impl.networking.cdn.CdnFileHandlerImpl;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.models.addon.annotation.AddonMain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@AddonMain
public class PriceCxnAddon extends LabyAddon<PriceCxnConfiguration> implements Mod {
    public static final Style DEFAULT_STYLE = Style.EMPTY.color(NamedTextColor.GRAY);
    public static final Component MOD_TEXT = Component
            .text(Component.empty())
            .append(Component.text(Component.text("["))
                    .style(Style.EMPTY.color(NamedTextColor.DARK_GRAY)))
            .append(Component.translatable("cxn_listener.mod_text")
                    .style(Style.EMPTY.color(NamedTextColor.GOLD)))
            .append(Component.text(Component.text("] "))
                    .style(Style.EMPTY.color(NamedTextColor.DARK_GRAY)));

    private final Map<Class<? extends KeybindExecutor>, IKeyBinding> classKeyBindingMap = new HashMap<>();
    private final Map<IKeyBinding, KeybindExecutor> keyBindingKeybindExecutorMap = new HashMap<>();
    private final ModuleLoader projectLoader;

    private final CxnListener cxnListener;
    private final CdnFileHandler cdnFileHandler;
    private final Http http;
    private final TickListener tickListener;
    private PriceCxnItemStack.ViewMode viewMode = PriceCxnItemStack.ViewMode.CURRENT_STACK;
    private final ILogger logger = new LoggerImpl(logger());

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
        this.registerListener(tickListener);

        try {
            cxnListener = new CxnListener();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.projectLoader.addModule(new ClasspathModule("de.alive.preiscxn.api"));
        this.projectLoader.addModule(new ClasspathModule("de.alive.scanner.inventory"));

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

        this.registerListener(cxnListener);

        this.logger().info("Enabled the Addon");
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
        return MOD_TEXT;
    }

    @Override
    public void printTester(String message) {
        if (!DEBUG_MODE && !TESTER_MODE) return;

        if (labyAPI().minecraft() == null) return;

        if (labyAPI().minecraft().chatExecutor() != null)
            labyAPI().minecraft().chatExecutor().insertText(message, true);

        PriceCxn.getMod().getLogger().debug("[PCXN-TESTER] : {}", message);
    }

    @Override
    public void printDebug(String message, boolean overlay, boolean sysOut) {
        if (!DEBUG_MODE && !TESTER_MODE) return;
        if (labyAPI().minecraft() == null) return;
        if (labyAPI().minecraft().getClientPlayer() == null) return;

        labyAPI().minecraft().chatExecutor().insertText(message, true);
        if (sysOut) PriceCxn.getMod().getLogger().debug("[PCXN-DEBUG] : {}", message);
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

    @Override
    public void registerKeybinding(int code, String translationKey, String category, @NotNull KeybindExecutor keybindExecutor, boolean inInventory) {
        IKeyBinding keyBinding = getLabyEntrypoint()
                .createKeyBinding(code, translationKey, category, keybindExecutor, inInventory);

        classKeyBindingMap.put(keybindExecutor.getClass(), keyBinding);
        if (keyBinding.isInInventory())
            keyBindingKeybindExecutorMap.put(keyBinding, keybindExecutor);

        tickListener.addTickConsumer(client -> {
            if (keyBinding.wasPressed() && !client.isPlayerNull()) {

                keybindExecutor.onKeybindPressed(
                        getLabyEntrypoint().createMinecraftClient(),
                        getLabyEntrypoint().createInventory().getMainHandStack()
                );
            }
        });
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
    public void openUrl(String url) {

    }

    @Override
    public void printChat(Object message) {

    }

    private LabyEntrypoint getLabyEntrypoint() {
        return ((DefaultReferenceStorage) this.referenceStorageAccessor()).labyEntrypoint();
    }
}
