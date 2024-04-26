package de.alive.preiscxn.inventory.scanner;

import de.alive.api.module.PriceCxnModule;

public class InventoryScanner implements PriceCxnModule {
    @Override
    public void loadModule() {
        System.out.println("InventoryScanner loaded");
    }
}
