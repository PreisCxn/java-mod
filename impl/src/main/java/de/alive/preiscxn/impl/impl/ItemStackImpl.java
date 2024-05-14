package de.alive.preiscxn.impl.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.interfaces.IItemStack;
import de.alive.api.networking.DataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData,
                                             boolean addComment) {
        return PriceCxn.getMod().createItemStack(this, searchData, addComment);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData) {
        return PriceCxn.getMod().createItemStack(this, searchData);
    }

    @Override
    public PriceCxnItemStack createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {
        return PriceCxn.getMod().createItemStack(this, searchData, addComment, addTooltips);
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
    public String getItemName() {
        return stack.getItem().getTranslationKey();
    }

    @Override
    public String getDisplayName() {
        return stack.getName().getString();
    }

    @Override
    public int getCount() {
        return stack.getCount();
    }

    @Override
    public boolean isTrimTemplate() {
        return stack.isIn(ItemTags.TRIM_TEMPLATES);
    }

    @Override
    public boolean isNetheriteUpgradeSmithingTemplate() {
        return stack.isOf(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
    }

    @Override
    public Optional<String> getRegistryKey() {
        return stack.getRegistryEntry().getKey()
                .map(itemRegistryKey -> itemRegistryKey.getValue().getPath());
    }

    @Override
    public JsonObject getComponentsAsJson() {
        return componentMapToJson(stack.getComponents());
    }

    private @NotNull JsonObject componentMapToJson(@NotNull ComponentMap componentMap) {
        if(componentMap.isEmpty())
            return new JsonObject();

        JsonObject json = new JsonObject();

        for (DataComponentType<?> key : componentMap.getTypes()) {
            Object component = componentMap.get(key);
            switch (component) {
                case null -> {
                }
                case ComponentMap subComponentMap -> json.add(key.toString(), componentMapToJson(subComponentMap));
                case NbtComponent subComponentMap -> {
                    try {
                        JsonObject asJsonObject = JsonParser.parseString(subComponentMap.toString()).getAsJsonObject();
                        json.add(key.toString(), asJsonObject);
                    } catch (JsonParseException e) {
                        try {
                            JsonArray asJsonObject = JsonParser.parseString(subComponentMap.toString()).getAsJsonArray();
                            json.add(key.toString(), asJsonObject);
                        } catch (JsonParseException e1) {
                            json.addProperty(key.toString(), subComponentMap.toString());
                        }
                    }
                }
                default -> {
                    Object object = object(component.toString());
                    if (object instanceof JsonElement element)
                        json.add(key.toString(), element);
                    else
                        json.addProperty(key.toString(), object.toString());
                }
            }

        }

        return json;
    }

    public Object object(String nbtString) {
        if (nbtString == null)
            return "";

        nbtString = TO_DELETE_PATTERN.matcher(nbtString).replaceAll("");

        JsonObject valueJson;

        //test if only Delete Pattern is needed
        try {
            valueJson = JsonParser.parseString(nbtString).getAsJsonObject();
        } catch (IllegalStateException e) {
            nbtString = JSON_KEY_PATTERN.matcher(nbtString).replaceAll("$1\"$2\":");

            //test if JsonArray
            try {
                return JsonParser.parseString(nbtString).getAsJsonArray();
            } catch (IllegalStateException ignored) {
                //test if JsonKey is missing
                try {
                    return JsonParser.parseString(nbtString).getAsJsonObject();
                } catch (IllegalStateException e2) {
                    //else add as normal String
                    return nbtString;
                }
            }
        } catch (JsonParseException e) {
            //else add as normal String
            return nbtString;
        }

        if (valueJson != null) {
            return valueJson;
        }

        return nbtString;
    }

    @Override
    public String getTranslationKey() {
        return stack.getTranslationKey();
    }

    @Override
    public List<String> getTooltip() {
        List<Text> tooltip = stack.getTooltip(Item.TooltipContext.DEFAULT,
                MinecraftClient.getInstance().player,
                MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC);
        List<String> lore = new ArrayList<>();
        tooltip.forEach(text -> lore.add(text.getString()));
        return lore;
    }
}
