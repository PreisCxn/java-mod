package de.alive.preiscxn.fabric;

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
import de.alive.preiscxn.api.interfaces.VersionedTabGui;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import de.alive.preiscxn.api.module.Module;
import de.alive.preiscxn.api.module.ModuleLoader;
import de.alive.preiscxn.api.module.PriceCxnModule;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.api.networking.Http;
import de.alive.preiscxn.api.networking.cdn.CdnFileHandler;
import de.alive.preiscxn.fabric.impl.GameHudImpl;
import de.alive.preiscxn.fabric.impl.InventoryImpl;
import de.alive.preiscxn.fabric.impl.ItemStackImpl;
import de.alive.preiscxn.fabric.impl.KeyBindingImpl;
import de.alive.preiscxn.fabric.impl.LoggerImpl;
import de.alive.preiscxn.fabric.impl.MinecraftClientImpl;
import de.alive.preiscxn.fabric.impl.PlayerImpl;
import de.alive.preiscxn.fabric.impl.PriceTextImpl;
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
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PriceCxnModClient implements ClientModInitializer, Mod {
    private static final String MOD_NAME = "PriceCxn";
    public final ILogger logger = new LoggerImpl(LoggerFactory.getLogger("PriceCxn"));

    private final Map<Class<? extends KeybindExecutor>, IKeyBinding> classKeyBindingMap = new HashMap<>();
    private final Map<IKeyBinding, KeybindExecutor> keyBindingKeybindExecutorMap = new HashMap<>();
    private final ModuleLoader projectLoader;

    private final CxnListener cxnListener;
    private final CdnFileHandler cdnFileHandler;
    private final Http http;

    private PriceCxnItemStack.ViewMode viewMode = PriceCxnItemStack.ViewMode.CURRENT_STACK;

    public PriceCxnModClient() {
        this.http = new HttpImpl();
        this.cdnFileHandler = new CdnFileHandlerImpl(http);
        try {
            Field mod = PriceCxn.class.getDeclaredField("mod");
            mod.setAccessible(true);
            mod.set(null, this);
            mod.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        PriceCxn.getMod().getLogger().info("PriceCxn client created");

        this.projectLoader = new ModuleLoaderImpl();

        this.projectLoader.addModule(new MainModule());

        try {
            cxnListener = new CxnListener();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.projectLoader.addModule(new ClasspathModule("de.alive.preiscxn.api", Thread.currentThread().getContextClassLoader()));
        this.projectLoader.addModule(new ClasspathModule("de.alive.preiscxn.inventoryscanner", Thread.currentThread().getContextClassLoader()));

        registerRemoteModule(
                "de.alive.preiscxn.listener.inventory.AuctionHouseListener",
                "Listener.jar",
                Path.of("./downloads/" + MOD_NAME + "_modules/cxn.listener.jar"),
                "de.alive.preiscxn.listener")
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
    public void onInitializeClient() {
        PriceCxn.getMod().getLogger().info("PriceCxn client initialized");

        registerKeybinding(
                GLFW.GLFW_KEY_H,
                "cxn_listener.keys.open_in_browser",
                "cxn_listener.mod_text",
                new OpenBrowserKeybindExecutor(),
                true);
        registerKeybinding(
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                "cxn_listener.keys.cycle_amount",
                "cxn_listener.mod_text",
                new SwitchItemViewKeybindExecutor(),
                true);

    }

    @Override
    public String getVersion() {
        return Version.MOD_VERSION;
    }

    @Override
    public Style getDefaultStyle() {
        return Style.EMPTY.withColor(Formatting.GRAY);
    }

    @Override
    public Text getModText() {
        return  MutableText
                .of(new PlainTextContent.Literal(""))
                .append(MutableText.of(new PlainTextContent.Literal("["))
                        .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                .append(Text.translatable("cxn_listener.mod_text")
                        .setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                .append(MutableText.of(new PlainTextContent.Literal("] "))
                        .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
    }

    @Override
    public void printTester(String message) {
        if (!DEBUG_MODE && !TESTER_MODE) return;
        if (MinecraftClient.getInstance() == null) return;
        if (MinecraftClient.getInstance().player == null) return;
        MinecraftClient client = MinecraftClient.getInstance();

        MutableText text = MutableText.of(new PlainTextContent.Literal(message));
        client.player.sendMessage(text, true);
        PriceCxn.getMod().getLogger().debug("[PCXN-TESTER] : {}", message);
    }

    @Override
    public void printDebug(String message, boolean overlay, boolean sysOut) {
        if (!DEBUG_MODE) return;
        if (MinecraftClient.getInstance() == null) return;
        if (MinecraftClient.getInstance().player == null) return;
        MinecraftClient client = MinecraftClient.getInstance();

        MutableText text = MutableText.of(new PlainTextContent.Literal(message)).setStyle(Style.EMPTY.withColor(Formatting.RED).withItalic(true));
        if (client.player != null)
            client.player.sendMessage(text, overlay);
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
        return Text.of(" ");
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
        return InventoryImpl.getInstance(MinecraftClient.getInstance());
    }

    @Override
    public ICxnListener getCxnListener() {
        return cxnListener;
    }

    @Override
    public IPlayer getPlayer() {
        return new PlayerImpl();
    }

    @Override
    public void runOnEndClientTick(Consumer<IMinecraftClient> consumer) {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client != null) {
                consumer.accept(MinecraftClientImpl.getInstance(client));
            }
        });
    }

    @Override
    public void runOnJoin(Consumer<IMinecraftClient> consumer) {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client != null) {
                consumer.accept(MinecraftClientImpl.getInstance(client));
            }
        });
    }

    @Override
    public void runOnDisconnect(Consumer<IMinecraftClient> consumer) {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (client != null) {
                consumer.accept(MinecraftClientImpl.getInstance(client));
            }
        });
    }

    @Override
    public CdnFileHandler getCdnFileHandler() {
        return cdnFileHandler;
    }

    @Override
    public IMinecraftClient getMinecraftClient() {
        return MinecraftClientImpl.getInstance(MinecraftClient.getInstance());
    }

    @Override
    public Http getHttp() {
        return http;
    }

    private void registerKeybinding(int code, String translationKey, String category, @NotNull KeybindExecutor keybindExecutor, boolean inInventory) {
        IKeyBinding keyBinding = new KeyBindingImpl(
                KeyBindingHelper.registerKeyBinding(
                        new KeyBinding(translationKey, InputUtil.Type.KEYSYM, code, category)),
                keybindExecutor,
                inInventory);

        classKeyBindingMap.put(keybindExecutor.getClass(), keyBinding);
        if (inInventory)
            keyBindingKeybindExecutorMap.put(keyBinding, keybindExecutor);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.wasPressed() && client.player != null) {
                keybindExecutor.onKeybindPressed(
                        MinecraftClientImpl.getInstance(client),
                        ItemStackImpl.getInstance(client.player.getInventory().getMainHandStack())
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
        return logger;
    }

    @Override
    public IGameHud getGameHud() {
        return new GameHudImpl(MinecraftClient.getInstance().inGameHud);
    }

    @Override
    public VersionedTabGui getVersionedTabGui() {
        return (VersionedTabGui) MinecraftClient.getInstance().inGameHud.getPlayerListHud();
    }

    @Override
    public void openUrl(String url) {
        getMinecraftClient().openUrl(url);
    }
}
