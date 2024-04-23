package de.alive.pricecxn;

import de.alive.pricecxn.cytooxien.CxnListener;
import de.alive.pricecxn.cytooxien.ICxnListener;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.PriceCxnItemStackImpl;
import de.alive.pricecxn.impl.MinecraftClientImpl;
import de.alive.pricecxn.interfaces.IMinecraftClient;
import de.alive.pricecxn.interfaces.IPlayer;
import de.alive.pricecxn.interfaces.Mod;
import de.alive.pricecxn.keybinds.KeybindExecutor;
import de.alive.pricecxn.keybinds.OpenBrowserKeybindExecutor;
import de.alive.pricecxn.modules.ModuleLoader;
import de.alive.pricecxn.networking.DataAccess;
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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static de.alive.pricecxn.LogPrinter.LOGGER;
import static de.alive.pricecxn.PriceCxnMod.MOD_NAME;

public class PriceCxnModClient implements ClientModInitializer, Mod {
    private static final Callable<Package> DEFAULT_PACKAGE = () -> {

        try {
            Class<?> aClass = Class.forName("de.alive.preiscxn.inventory.listener.AuctionHouseListener");
            LogPrinter.LOGGER.info("Found listener package: {}", aClass.getPackage());
            return aClass.getPackage();
        }catch (Exception e){
            LogPrinter.LOGGER.info("Failed to get default package, assuming this is no dev environment");
            return null;
        }
    };
    private final CxnListener CXN_LISTENER;

    public PriceCxnModClient(){
        try {
            Field mod = Class.forName("de.alive.pricecxn.PriceCxn").getDeclaredField("mod");
            mod.setAccessible(true);
            mod.set(null, this);
            mod.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("PriceCxn client created");

        try {
            CXN_LISTENER = new CxnListener(new ModuleLoader(
                    DEFAULT_PACKAGE.call(),
                    "modules/cxn.listener.jar",
                    Path.of("./" + MOD_NAME + "_modules/cxn.listener.jar")));
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
}
