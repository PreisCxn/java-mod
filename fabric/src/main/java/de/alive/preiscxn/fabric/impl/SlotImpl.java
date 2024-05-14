package de.alive.preiscxn.fabric.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.interfaces.ISlot;
import de.alive.api.networking.DataAccess;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public final class SlotImpl implements ISlot {
    private static final Cache<Slot, SlotImpl> SLOT_MAP = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .build();
    private final Slot slot;

    private SlotImpl(Slot slot) {
        this.slot = slot;
    }

    public static SlotImpl getInstance(Slot slot) {
        try {
            return SLOT_MAP.get(slot, () -> new SlotImpl(slot));
        } catch (ExecutionException e) {
            return new SlotImpl(slot);
        }
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment) {
        return PriceCxn.getMod().createItemStack(ItemStackImpl.getInstance(slot.getStack()), searchData, addComment);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData) {
        return PriceCxn.getMod().createItemStack(ItemStackImpl.getInstance(slot.getStack()), searchData);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {
        return PriceCxn.getMod().createItemStack(ItemStackImpl.getInstance(slot.getStack()), searchData, addComment, addTooltips);
    }

    @Override
    public boolean isStackNbtNull() {
        return slot.getStack() == null || slot.getStack().getComponents() == null;
    }

    @Override
    public int stackNameHash() {
        return slot.getStack().getName().hashCode();
    }

    @Override
    public int stackNbtHash() {
        return slot.getStack().getComponents() == null ? 0 : slot.getStack().getComponents().hashCode();
    }

    @Override
    public boolean isStackEmpty() {
        return slot.getStack().isEmpty();
    }

    @Override
    public boolean isStackNull() {
        return slot.getStack() == null;
    }
}
