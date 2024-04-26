package de.alive.pricecxn.impl;

import de.alive.api.interfaces.IItemStack;
import net.minecraft.item.ItemStack;

public class ItemStackImpl implements IItemStack {
    private final ItemStack stack;

    public ItemStackImpl(ItemStack stack) {
        this.stack = stack;
    }
    @Override
    public ItemStack getStack() {
        return stack;
    }
}
