package de.alive.preiscxn.inventoryscanner;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.keybinds.CustomKeyBinding;
import de.alive.preiscxn.api.module.PriceCxnModule;
import de.alive.preiscxn.inventoryscanner.keybinds.ScanInventoryKeybind;

public class InventoryScanner implements PriceCxnModule {
    @Override
    public void loadModule() {
        PriceCxn.getMod().getLogger().info("InventoryScanner loaded");
        PriceCxn.getMod()
                .registerKeybinding(
                        CustomKeyBinding.GLFW_KEY_M,
                        "a",
                        "b",
                        new ScanInventoryKeybind(),
                        true);
    }
}
