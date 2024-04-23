package de.alive.pricecxn.impl;

import de.alive.pricecxn.interfaces.IScreenHandler;
import de.alive.pricecxn.interfaces.ISlot;
import net.minecraft.screen.ScreenHandler;

public class ScreenHandlerImpl implements IScreenHandler {
    private final ScreenHandler screenHandler;

    public ScreenHandlerImpl(ScreenHandler screenHandler) {
        this.screenHandler = screenHandler;
    }
    @Override
    public ISlot getSlot(int i) {
        return screenHandler.getSlot(i) == null ? null : new SlotImpl(screenHandler.getSlot(i));
    }
}
