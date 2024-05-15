package de.alive.preiscxn.v1_20_6;

import de.alive.preiscxn.api.interfaces.ISlot;
import de.alive.preiscxn.core.impl.LabyScreenHandler;
import net.labymod.api.models.Implements;
import net.minecraft.world.inventory.AbstractContainerMenu;

@Implements(LabyScreenHandler.class)
public final class ScreenHandlerImpl implements LabyScreenHandler {
    private AbstractContainerMenu screenHandler;

    public ScreenHandlerImpl() {
    }

    public ScreenHandlerImpl setScreenHandler(AbstractContainerMenu screenHandler) {
        this.screenHandler = screenHandler;
        return this;
    }

    @Override
    public ISlot getSlot(int i) {
        screenHandler.getSlot(i);
        return new SlotImpl().setSlot(screenHandler.getSlot(i));
    }
}
