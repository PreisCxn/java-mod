package de.alive.preiscxn;

import de.alive.api.Mod;
import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.ICxnListener;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.interfaces.IPlayer;
import de.alive.api.keybinds.KeybindExecutor;
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
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

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

        this.projectLoader =
                new ModuleLoaderImpl(
                        Thread.currentThread().getContextClassLoader(),
                        Thread.currentThread().getContextClassLoader().getDefinedPackage("de.alive.preiscxn"));

        this.projectLoader.addModule(new MainModule());

        try {
            cxnListener = new CxnListener();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        RemoteModule.create("Listener.jar",
                        Path.of("./downloads/" + MOD_NAME + "_modules/cxn.listener.jar"))
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

    @Override
    public void onInitializeClient() {
        LOGGER.info("PriceCxn client initialized");

        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "cxn_listener.keys.open_in_browser",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "cxn_listener.mod_text"
        ));

        registerKeybinding(keyBinding, new OpenBrowserKeybindExecutor(), true);
    }

    @Override
    public PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {
        return new PriceCxnItemStackImpl(item, searchData, addComment, addTooltips);
    }

    @Override
    public PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment) {
        return new PriceCxnItemStackImpl(item, searchData, addComment);
    }

    @Override
    public PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData) {
        return new PriceCxnItemStackImpl(item, searchData);
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
                consumer.accept(new MinecraftClientImpl(client));
            }
        });
    }

    @Override
    public CdnFileHandler getCdnFileHandler() {
        return cdnFileHandler;
    }

    @Override
    public IMinecraftClient getMinecraftClient() {
        return new MinecraftClientImpl(MinecraftClient.getInstance());
    }

    @Override
    public Http getHttp() {
        return http;
    }

    @Override
    public void registerKeybinding(@NotNull KeyBinding keyBinding, @NotNull KeybindExecutor keybindExecutor, boolean inInventory) {
        classKeyBindingMap.put(keybindExecutor.getClass(), keyBinding);
        if (inInventory)
            keyBindingKeybindExecutorMap.put(keyBinding, keybindExecutor);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.wasPressed() && client.player != null) {
                keybindExecutor.onKeybindPressed(
                        new MinecraftClientImpl(client),
                        new ItemStackImpl(client.player.getInventory().getMainHandStack())
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

}
