package de.alive.pricecxn;

import de.alive.pricecxn.cytooxien.CxnListener;
import de.alive.pricecxn.keybinds.KeybindExecutor;
import de.alive.pricecxn.keybinds.OpenBrowserKeybindExecutor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PriceCxnModClient implements ClientModInitializer {

    public static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    public static final CxnListener CXN_LISTENER = new CxnListener();

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
