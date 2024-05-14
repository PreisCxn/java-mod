package de.alive.preiscxn.v1_20_5.impl;

import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.interfaces.ISlot;
import de.alive.api.networking.DataAccess;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class SlotImpl implements ISlot {
    private final Slot slot;

    public SlotImpl(Slot slot) {
        this.slot = slot;
    }


    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment) {
        return PriceCxn.getMod().createItemStack(ItemStackImpl.getInstance(slot.getItem()), searchData, addComment);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData) {
        return PriceCxn.getMod().createItemStack(ItemStackImpl.getInstance(slot.getItem()), searchData);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {
        return PriceCxn.getMod().createItemStack(ItemStackImpl.getInstance(slot.getItem()), searchData, addComment, addTooltips);
    }

    @Override
    public boolean isStackNbtNull() {
        return false;
    }

    @Override
    public int stackNameHash() {
        return slot.getItem().getDisplayName().hashCode();
    }

    @Override
    public int stackNbtHash() {
        slot.getItem();
        return slot.getItem().getComponents().hashCode();
    }

    @Override
    public boolean isStackEmpty() {
        return slot.getItem().isEmpty();
    }

    @Override
    public boolean isStackNull() {
        return false;
    }
}
