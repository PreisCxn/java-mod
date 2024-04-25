package de.alive.api.impl;

import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.interfaces.ISlot;
import de.alive.api.networking.DataAccess;
import de.alive.api.PriceCxn;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SlotImpl implements ISlot {
    private final Slot slot;

    public SlotImpl(Slot slot) {
        this.slot = slot;
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment) {
        return PriceCxn.getMod().createItemStack(slot.getStack(), searchData, addComment);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData) {
        return PriceCxn.getMod().createItemStack(slot.getStack(), searchData);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {
        return PriceCxn.getMod().createItemStack(slot.getStack(), searchData, addComment, addTooltips);
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
