package de.alive.pricecxn;

import de.alive.preiscxn.inventory.listener.AuctionHouseListener;
import de.alive.pricecxn.cytooxien.CxnListener;
import de.alive.pricecxn.cytooxien.ICxnListener;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import de.alive.pricecxn.cytooxien.PriceCxnItemStackImpl;
import de.alive.pricecxn.keybinds.KeybindExecutor;
import de.alive.pricecxn.keybinds.OpenBrowserKeybindExecutor;
import de.alive.pricecxn.modules.ModuleLoader;
import de.alive.pricecxn.networking.DataAccess;
import net.fabricmc.api.ClientModInitializer;
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

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static de.alive.pricecxn.PriceCxnMod.MOD_NAME;

public class PriceCxnModClient implements ClientModInitializer, Mod {
    private static final Callable<Tuple2<ClassLoader, Package>> DEFAULT_PACKAGE = () -> {

        try {
            new AuctionHouseListener();
            return Tuples.of(AuctionHouseListener.class.getClassLoader(), AuctionHouseListener.class.getPackage());
        }catch (Exception e){
            LogPrinter.LOGGER.error("Failed to get default package", e);
            return null;
        }
    };
    public static final CxnListener CXN_LISTENER;

    static {
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
        //Initialize Mod

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
    public boolean isPlayerNull() {
        return MinecraftClient.getInstance().player == null;
    }

    @Override
    public boolean isCurrentScreenHandlerNull() {
        return MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().player.currentScreenHandler == null;
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
}
