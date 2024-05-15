package de.alive.preiscxn.inventoryscanner;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.keybinds.CustomKeyBinding;
import de.alive.preiscxn.api.module.PriceCxnModule;
import de.alive.preiscxn.inventoryscanner.keybinds.ScanInventoryKeybind;

import static de.alive.preiscxn.api.LogPrinter.LOGGER;

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
