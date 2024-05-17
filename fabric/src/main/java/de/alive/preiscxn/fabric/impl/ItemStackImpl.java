package de.alive.preiscxn.fabric.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

    public JsonObject priceCxn$getComponentsAsJson() {
        if (stack.getNbt() == null)
            return new JsonObject();
        return componentMapToJson(stack.getNbt());
    }

    private @NotNull JsonObject componentMapToJson(NbtCompound componentMap) {
        if (componentMap.isEmpty())
            return new JsonObject();

        JsonObject json = new JsonObject();

        for (String key : componentMap.getKeys()) {
            Object component = componentMap.get(key);
            switch (component) {
                case null -> {
                }
                case NbtCompound subComponentMap -> json.add(key, componentMapToJson(subComponentMap));
                case NbtElement subComponentMap -> {
                    try {
                        JsonObject asJsonObject = JsonParser.parseString(subComponentMap.toString()).getAsJsonObject();
                        json.add(key, asJsonObject);
                    } catch (JsonParseException | IllegalStateException e) {
                        try {
                            JsonArray asJsonObject = JsonParser.parseString(subComponentMap.toString()).getAsJsonArray();
                            json.add(key, asJsonObject);
                        } catch (JsonParseException | IllegalStateException e1) {
                            Object object = object(component.toString());
                            if (object instanceof JsonElement element)
                                json.add(key, element);
                            else
                                json.addProperty(key, object.toString());
                        }
                    }
                }
                default -> {
                }
            }

        }

        return json;
    }

    public Object object(String nbtString) {
        if (nbtString == null)
            return "";

        if(!nbtString.matches("[\\[\\]{}]") && nbtString.contains("\""))
            nbtString = nbtString.replace("\"", "");

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
}
