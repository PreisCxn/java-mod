package de.alive.preiscxn.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.alive.api.interfaces.IScreenHandler;
import de.alive.api.interfaces.ISlot;
import net.minecraft.screen.ScreenHandler;

import java.util.concurrent.ExecutionException;

public class ScreenHandlerImpl implements IScreenHandler {
    private static final Cache<ScreenHandler, ScreenHandlerImpl> screenHandlerMap = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .build();
    private final ScreenHandler screenHandler;

    private ScreenHandlerImpl(ScreenHandler screenHandler) {
        this.screenHandler = screenHandler;
    }

    public static ScreenHandlerImpl getInstance(ScreenHandler screenHandler) {
        try {
            return screenHandlerMap.get(screenHandler, () -> new ScreenHandlerImpl(screenHandler));
        } catch (ExecutionException e) {
            return new ScreenHandlerImpl(screenHandler);
        }
    }

    @Override
    public ISlot getSlot(int i) {
        return screenHandler.getSlot(i) == null ? null : SlotImpl.getInstance(screenHandler.getSlot(i));
    }
}
