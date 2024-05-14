package de.alive.preiscxn.v1_20_5.impl;

import de.alive.api.interfaces.IScreenHandler;
import de.alive.api.interfaces.ISlot;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class ScreenHandlerImpl implements IScreenHandler {
    private final AbstractContainerMenu screenHandler;

    public ScreenHandlerImpl(AbstractContainerMenu screenHandler) {
        this.screenHandler = screenHandler;
    }

    @Override
    public ISlot getSlot(int i) {
        screenHandler.getSlot(i);
        return new SlotImpl(screenHandler.getSlot(i));
    }
}
