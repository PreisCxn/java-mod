package de.alive.preiscxn.inventoryscanner;

import de.alive.preiscxn.api.Mod;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.keybinds.CustomKeyBinding;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import de.alive.preiscxn.api.module.PriceCxnModule;
import de.alive.preiscxn.inventoryscanner.keybinds.ScanInventoryKeybind;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InventoryScanner implements PriceCxnModule {
    @Override
    public void loadModule() {
        PriceCxn.getMod().getLogger().info("InventoryScanner loaded");
        Mod mod = PriceCxn.getMod();

        Class<? extends Mod> aClass = mod.getClass();
        try {
            Method registerKeybinding = aClass.getDeclaredMethod("registerKeybinding", int.class, String.class, String.class, KeybindExecutor.class, boolean.class);
            registerKeybinding.setAccessible(true);
            registerKeybinding.invoke(mod, CustomKeyBinding.GLFW_KEY_M, "a", "b", new ScanInventoryKeybind(), true);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            mod.getLogger().error("Failed to register keybinding", e);
        }
    }
}
