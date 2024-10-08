package de.alive.preiscxn.fabric.v1_21.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.alive.preiscxn.api.interfaces.IScreenHandler;
import de.alive.preiscxn.api.interfaces.ISlot;
import net.minecraft.screen.ScreenHandler;

import java.util.concurrent.ExecutionException;

public final class ScreenHandlerImpl implements IScreenHandler {
    private static final Cache<ScreenHandler, ScreenHandlerImpl> SCREEN_HANDLER_MAP = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .build();
    private final ScreenHandler screenHandler;

    public ScreenHandlerImpl(ScreenHandler screenHandler) {
        this.screenHandler = screenHandler;
    }

    public static ScreenHandlerImpl getInstance(ScreenHandler screenHandler) {
        try {
            return SCREEN_HANDLER_MAP.get(screenHandler, () -> new ScreenHandlerImpl(screenHandler));
        } catch (ExecutionException e) {
            return new ScreenHandlerImpl(screenHandler);
        }
    }

    @Override
    public ISlot getSlot(int i) {
        return screenHandler.getSlot(i) == null ? null : SlotImpl.getInstance(screenHandler.getSlot(i));
    }
}
