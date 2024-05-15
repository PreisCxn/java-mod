package de.alive.preiscxn.v1_20_6.impl;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.core.impl.LabySlot;
import net.labymod.api.models.Implements;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Implements(LabySlot.class)
public final class SlotImpl implements LabySlot {
    private Slot slot;

    public SlotImpl() {
    }

    SlotImpl setSlot(Slot slot) {
        this.slot = slot;
        return this;
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment) {
        return PriceCxn.getMod().createItemStack(new ItemStackImpl().setStack(slot.getItem()), searchData, addComment);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData) {
        return PriceCxn.getMod().createItemStack(new ItemStackImpl().setStack(slot.getItem()), searchData);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {
        return PriceCxn.getMod().createItemStack(new ItemStackImpl().setStack(slot.getItem()), searchData, addComment, addTooltips);
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
