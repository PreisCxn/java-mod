package de.alive.pricecxn;

import de.alive.api.LogPrinter;
import de.alive.api.Mod;
import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.ICxnListener;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.interfaces.IMinecraftClient;
import de.alive.api.interfaces.IPlayer;
import de.alive.api.networking.DataAccess;
import de.alive.api.networking.Http;
import de.alive.api.networking.cdn.CdnFileHandler;
import de.alive.pricecxn.cytooxien.CxnListener;
import de.alive.pricecxn.cytooxien.PriceCxnItemStackImpl;
import de.alive.pricecxn.impl.MinecraftClientImpl;
import de.alive.pricecxn.keybinds.KeybindExecutor;
import de.alive.pricecxn.keybinds.OpenBrowserKeybindExecutor;
import de.alive.pricecxn.modules.ModuleLoader;
import de.alive.pricecxn.networking.HttpImpl;
import de.alive.pricecxn.networking.cdn.CdnFileHandlerImpl;
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
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static de.alive.api.LogPrinter.LOGGER;
import static de.alive.pricecxn.PriceCxnMod.MOD_NAME;

public class PriceCxnModClient implements ClientModInitializer, Mod {
    private static final Callable<Package> DEFAULT_PACKAGE = () -> {

        try {
            Package definedPackage = Thread.currentThread().getContextClassLoader().getDefinedPackage("de.alive.preiscxn.inventory.listener");
            LogPrinter.LOGGER.info("Found listener package: {}", definedPackage);
            return definedPackage;
        }catch (Exception e){
            LogPrinter.LOGGER.info("Failed to get default package, assuming this is no dev environment");
            return null;
        }
    };
    private final CxnListener CXN_LISTENER;
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

        try {
            CXN_LISTENER = new CxnListener(new ModuleLoader(
                    DEFAULT_PACKAGE.call(),
                    "Listener.jar",
                    Path.of("./downloads/" + MOD_NAME + "_modules/cxn.listener.jar")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        KeybindExecutor.register(keyBinding, new OpenBrowserKeybindExecutor(), true);
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
        return CXN_LISTENER;
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

}
