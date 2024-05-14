package de.alive.preiscxn.core;

import de.alive.api.Mod;
import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.ICxnConnectionManager;
import de.alive.api.cytooxien.ICxnListener;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.interfaces.IItemStack;
import de.alive.api.interfaces.IKeyBinding;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.interfaces.IPlayer;
import de.alive.api.keybinds.CustomKeyBinding;
import de.alive.api.keybinds.KeybindExecutor;
import de.alive.api.module.Module;
import de.alive.api.module.ModuleLoader;
import de.alive.api.module.PriceCxnModule;
import de.alive.api.networking.DataAccess;
import de.alive.api.networking.Http;
import de.alive.api.networking.cdn.CdnFileHandler;
import de.alive.preiscxn.core.events.TickListener;
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

import static de.alive.api.LogPrinter.LOGGER;

@AddonMain
public class PriceCxnAddon extends LabyAddon<PriceCxnConfiguration> implements Mod {
    public static final Style DEFAULT_TEXT = Style.EMPTY.color(NamedTextColor.GRAY);
    public static final String MOD_NAME = "PriceCxn";

    public static final String MOD_VERSION = Version.MOD_VERSION;

    private final Map<Class<? extends KeybindExecutor>, IKeyBinding> classKeyBindingMap = new HashMap<>();
    private final Map<IKeyBinding, KeybindExecutor> keyBindingKeybindExecutorMap = new HashMap<>();
    private final ModuleLoader projectLoader;

    private final CxnListener cxnListener;
    private final CdnFileHandler cdnFileHandler;
    private final Http http;
    private final TickListener tickListener;
    private PriceCxnItemStack.ViewMode viewMode = PriceCxnItemStack.ViewMode.CURRENT_STACK;

    public PriceCxnAddon() {
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

        LOGGER.info("PriceCxn client created");

        this.projectLoader = new ModuleLoaderImpl();

        this.projectLoader.addModule(new MainModule());

        try {
            cxnListener = new CxnListener();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.projectLoader.addModule(new ClasspathModule("de.alive.api"));
        this.projectLoader.addModule(new ClasspathModule("de.alive.scanner.inventory"));

        registerRemoteModule(
                "de.alive.inventory.listener.AuctionHouseListener",
                "Listener.jar",
                Path.of("./downloads/" + "MOD_NAME" + "_modules/cxn.listener.jar"),
                "de.alive.inventory")
                .doOnNext(module1 -> {
                    LOGGER.info("Adding module: {}", module1);
                    this.projectLoader.addModule(module1);
                    this.cxnListener.loadModules(this.projectLoader);

                    Set<Class<? extends PriceCxnModule>> classes1 = this.projectLoader.loadInterfaces(PriceCxnModule.class);
                    classes1.forEach(aClass -> {
                        LOGGER.info("Loading module: {}", aClass);
                        try {
                            aClass.getConstructor().newInstance().loadModule();
                            LOGGER.info("Loaded module: {}", aClass);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                                 | NoSuchMethodException e) {
                            LOGGER.error("Failed to load module: {}", aClass, e);
                        }
                    });
                }).subscribe();

        this.tickListener = new TickListener(this.getMinecraftClient());
    }

    private Mono<Module> registerRemoteModule(String classPath, String remotePath, Path localPath, String primaryPackage) {
        boolean useRemote;
        try {
            Thread.currentThread().getContextClassLoader().loadClass(classPath);
            useRemote = false;
        } catch (Exception e) {
            useRemote = true;
        }

        LOGGER.info("Registering remote module: {} ({}), local path: {}, primary package: {}, use remote: {}",
                classPath, remotePath, localPath, primaryPackage, useRemote);
        return RemoteModule.create(remotePath,
                localPath,
                primaryPackage,
                useRemote);
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
        return PriceCxnMod.MOD_VERSION;
    }

    @Override
    public Style getDefaultText() {
        return PriceCxnMod.DEFAULT_TEXT;
    }

    @Override
    public String getModText() {
        return PriceCxnMod.MOD_TEXT;
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
        return () -> {
            if (MinecraftClient.getInstance().player != null) {
                return MinecraftClient.getInstance().player.getName().getString();
            }
            return "";
        };
    }

    @Override
    public void runOnEndClientTick(Consumer<IMinecraftClient> consumer) {
        this.tickListener.add(consumer);
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

    @Override
    public void registerKeybinding(@NotNull CustomKeyBinding customKeyBinding, @NotNull KeybindExecutor keybindExecutor, boolean inInventory) {
        //todo: implement me
    /*IKeyBinding keyBinding = LabyKeyBinding.getInstance(KeyBindingHelper.registerKeyBinding(customKeyBinding.getKeybinding()));

    classKeyBindingMap.put(keybindExecutor.getClass(), keyBinding);
    if (inInventory)
      keyBindingKeybindExecutorMap.put(keyBinding, keybindExecutor);

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      if (keyBinding.wasPressed() && client.player != null) {
        keybindExecutor.onKeybindPressed(
                LabyMinecraftClient.getInstance(client),
                LabyItemStack.getInstance(client.player.getInventory().getMainHandStack())
        );
      }
    });*/
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

}
