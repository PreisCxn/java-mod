package de.alive.pricecxn;

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
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static de.alive.pricecxn.PriceCxnMod.MOD_NAME;

public class PriceCxnModClient implements ClientModInitializer, Mod {
    public static final CxnListener CXN_LISTENER = new CxnListener(new ModuleLoader("modules/cxn.listener.jar",
            Path.of("./" + MOD_NAME + "_modules/cxn.listener.jar")));

    public static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

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
}
