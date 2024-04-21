package de.alive.pricecxn;

import de.alive.pricecxn.cytooxien.CxnListener;
import de.alive.pricecxn.keybinds.KeybindExecutor;
import de.alive.pricecxn.keybinds.OpenBrowserKeybindExecutor;
import de.alive.pricecxn.modules.ModuleLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static de.alive.pricecxn.PriceCxnMod.MOD_NAME;

public class PriceCxnModClient implements ClientModInitializer {

    public static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    public static final CxnListener CXN_LISTENER = new CxnListener(new ModuleLoader("modules/cxn.listener.jar",
                                                                                    Path.of("./" + MOD_NAME + "_modules/cxn.listener.jar")));

    @Override
    public void onInitializeClient() {
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "cxn_listener.keys.open_in_browser",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "cxn_listener.mod_text"
        ));

        KeybindExecutor.register(keyBinding, new OpenBrowserKeybindExecutor(), true);
    }
}
