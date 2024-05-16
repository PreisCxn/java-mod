package de.alive.preiscxn.fabric.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.networking.DataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public final class ItemStackImpl implements IItemStack {
    private static final Pattern JSON_KEY_PATTERN = Pattern.compile("([{,])(\\w+):");
    private static final Pattern TO_DELETE_PATTERN = Pattern.compile("[\\\\']");
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
    public PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData,
                                                      boolean addComment) {
        return PriceCxn.getMod().createItemStack(this, searchData, addComment);
    }

    @Override
    public PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData) {
        return PriceCxn.getMod().createItemStack(this, searchData);
    }

    @Override
    public PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {
        return PriceCxn.getMod().createItemStack(this, searchData, addComment, addTooltips);
    }

    @Override
    public List<String> priceCxn$getLore() {
        List<Text> tooltip = stack.getTooltip(
                MinecraftClient.getInstance().player,
                MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipContext.ADVANCED : TooltipContext.BASIC);

        List<String> lore = new ArrayList<>();

        for (Text text : tooltip) {
            lore.add(text.getString());
        }

        return lore;
    }

    @Override
    public String priceCxn$getItemName() {
        return stack.getItem().getTranslationKey();
    }

    @Override
    public String priceCxn$getDisplayName() {
        return stack.getName().getString();
    }

    @Override
    public int priceCxn$getCount() {
        return stack.getCount();
    }

    @Override
    public boolean priceCxn$isTrimTemplate() {
        return stack.isIn(ItemTags.TRIM_TEMPLATES);
    }

    @Override
    public boolean priceCxn$isNetheriteUpgradeSmithingTemplate() {
        return stack.isOf(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
    }

    @Override
    public Optional<String> priceCxn$getRegistryKey() {
        return stack.getRegistryEntry().getKey()
                .map(itemRegistryKey -> itemRegistryKey.getValue().getPath());
    }

    @Override
    public @NotNull JsonObject priceCxn$getComponentsAsJson() {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return new JsonObject();

        return nbtToJson(nbt);
    }

    private @NotNull JsonObject nbtToJson(@NotNull NbtCompound nbt) {
        JsonObject json = new JsonObject();

        for (String key : nbt.getKeys()) {
            NbtElement nbtElement = nbt.get(key);
            if (nbtElement == null)
                continue;

            if(nbtElement instanceof NbtCompound nbtCompound){
                json.add(key, nbtToJson(nbtCompound));
            } else {
                String nbtString = nbtElement.asString();

                if (nbtString == null) continue;

                nbtString = TO_DELETE_PATTERN.matcher(nbtString).replaceAll("");

                JsonObject valueJson = null;

                //test if only Delete Pattern is needed
                try {
                    valueJson = JsonParser.parseString(nbtString).getAsJsonObject();
                } catch (IllegalStateException e) {
                    nbtString = JSON_KEY_PATTERN.matcher(nbtString).replaceAll("$1\"$2\":");

                    //test if JsonArray
                    try {
                        JsonArray array = JsonParser.parseString(nbtString).getAsJsonArray();
                        json.add(key, array);
                        continue;
                    } catch (IllegalStateException ignored) {
                    }

                    //test if JsonKey is missing
                    try {
                        valueJson = JsonParser.parseString(nbtString).getAsJsonObject();
                    } catch (IllegalStateException e2) {
                        //else add as normal String
                        json.addProperty(key, Optional.of(nbtElement).map(NbtElement::asString).orElse("null"));
                    }

                } catch (JsonParseException e) {
                    //else add as normal String
                    json.addProperty(key, Optional.of(nbtElement).map(NbtElement::asString).orElse("null"));
                }

                if (valueJson != null) {
                    json.add(key, valueJson);
                }
            }
        }

        return json;
    }

}
