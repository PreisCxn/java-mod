package de.alive.preiscxn.inventoryscanner;

import de.alive.api.PriceCxn;
import de.alive.api.keybinds.CustomKeyBinding;
import de.alive.api.module.PriceCxnModule;
import de.alive.preiscxn.inventoryscanner.keybinds.ScanInventoryKeybind;

import static de.alive.api.LogPrinter.LOGGER;

public class InventoryScanner implements PriceCxnModule {
    @Override
    public void loadModule() {
        LOGGER.info("InventoryScanner loaded");
        PriceCxn.getMod()
                .registerKeybinding(
                        new CustomKeyBinding("a", CustomKeyBinding.GLFW_KEY_M, "b"),
                        new ScanInventoryKeybind(),
                        true);
    }
}
