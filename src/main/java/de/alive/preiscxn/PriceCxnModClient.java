package de.alive.preiscxn;

import de.alive.api.Mod;
import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.ICxnListener;
import de.alive.api.cytooxien.PriceCxnItemStack;
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
import de.alive.preiscxn.cytooxien.CxnListener;
import de.alive.preiscxn.cytooxien.PriceCxnItemStackImpl;
import de.alive.preiscxn.impl.ItemStackImpl;
import de.alive.preiscxn.impl.MinecraftClientImpl;
import de.alive.preiscxn.keybinds.OpenBrowserKeybindExecutor;
import de.alive.preiscxn.keybinds.SwitchItemViewKeybindExecutor;
import de.alive.preiscxn.modules.ClasspathModule;
import de.alive.preiscxn.modules.MainModule;
import de.alive.preiscxn.modules.ModuleLoaderImpl;
import de.alive.preiscxn.modules.RemoteModule;
import de.alive.preiscxn.networking.HttpImpl;
import de.alive.preiscxn.networking.cdn.CdnFileHandlerImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
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
import static de.alive.preiscxn.PriceCxnMod.MOD_NAME;

public class PriceCxnModClient implements ClientModInitializer, Mod {
    private final Map<Class<? extends KeybindExecutor>, KeyBinding> classKeyBindingMap = new HashMap<>();
    private final Map<KeyBinding, KeybindExecutor> keyBindingKeybindExecutorMap = new HashMap<>();
    private final ModuleLoader projectLoader;

    private final CxnListener cxnListener;
    private final CdnFileHandler cdnFileHandler;
    private final Http http;

    private PriceCxnItemStack.ViewMode viewMode = PriceCxnItemStack.ViewMode.CURRENT_STACK;

    public PriceCxnModClient(){
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
                Path.of("./downloads/" + MOD_NAME + "_modules/cxn.listener.jar"),
                "de.alive.inventory")
                .doOnNext(module1 -> {
                    this.projectLoader.addModule(module1);
                    this.cxnListener.loadModules(this.projectLoader);

                    Set<Class<? extends PriceCxnModule>> classes1 = this.projectLoader.loadInterfaces(PriceCxnModule.class);
                    classes1.forEach(aClass -> {
                        try {
                            aClass.getConstructor().newInstance().loadModule();
                        LOGGER.info("Loaded module: {}", aClass);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException e) {
                            LOGGER.error("Failed to load module: {}", aClass, e);
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

        return RemoteModule.create(remotePath,
                localPath,
                primaryPackage,
                useRemote);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("PriceCxn client initialized");

        registerKeybinding(new CustomKeyBinding(
                "cxn_listener.keys.open_in_browser",
                GLFW.GLFW_KEY_H,
                "cxn_listener.mod_text"
        ), new OpenBrowserKeybindExecutor(), true);
        registerKeybinding(new CustomKeyBinding(
                "cxn_listener.keys.cycle_amount",
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                "cxn_listener.mod_text"
        ), new SwitchItemViewKeybindExecutor(), true);

    }

    @Override
    public PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {
        return PriceCxnItemStackImpl.getInstance(item, searchData, addComment, addTooltips);
    }

    @Override
    public PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment) {
        return PriceCxnItemStackImpl.getInstance(item, searchData, addComment);
    }

    @Override
    public PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData) {
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
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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

    @Override
    public void registerKeybinding(@NotNull CustomKeyBinding customKeyBinding, @NotNull KeybindExecutor keybindExecutor, boolean inInventory) {
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(customKeyBinding.getKeybinding());

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
    public KeyBinding getKeyBinding(Class<? extends KeybindExecutor> keybindExecutorClass) {
        return classKeyBindingMap.get(keybindExecutorClass);
    }

    @Override
    public void forEachKeybindExecutor(BiConsumer<? super KeyBinding, ? super KeybindExecutor> keyBinding) {
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

}
