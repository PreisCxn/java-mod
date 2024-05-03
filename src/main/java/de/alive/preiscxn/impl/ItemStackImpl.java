package de.alive.preiscxn.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.interfaces.IItemStack;
import de.alive.api.networking.DataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public final class ItemStackImpl implements IItemStack {
    private static final Cache<ItemStack, ItemStackImpl> ITEM_STACK_MAP = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .build();
    private final ItemStack stack;

    private ItemStackImpl(ItemStack stack) {
        this.stack = stack;
    }

    public static ItemStackImpl getInstance(ItemStack stack) {
        try {
            return ITEM_STACK_MAP.get(stack, () -> new ItemStackImpl(stack));
        } catch (ExecutionException e) {
            return new ItemStackImpl(stack);
        }
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData,
                                             boolean addComment) {
        return PriceCxn.getMod().createItemStack(stack, searchData, addComment);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData) {
        return PriceCxn.getMod().createItemStack(stack, searchData);
    }

    @Override
    public List<String> getLore() {
        List<Text> tooltip = stack.getTooltip(Item.TooltipContext.DEFAULT,
                MinecraftClient.getInstance().player,
                MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC);

        List<String> lore = new ArrayList<>();

        for (Text text : tooltip) {
            lore.add(text.getString());
        }

        return lore;
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {
        return PriceCxn.getMod().createItemStack(stack, searchData, addComment, addTooltips);
    }
}
