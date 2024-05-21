package de.alive.preiscxn.v1_20_4.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.core.impl.LabyItemStack;
import net.labymod.api.component.data.NbtDataComponentContainer;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Implements(LabyItemStack.class)
public final class ItemStackImpl implements LabyItemStack {
    private ItemStack stack;

    public ItemStackImpl() {
    }

    public ItemStackImpl setStack(ItemStack stack) {
        this.stack = stack;
        return this;
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
        List<Component> tooltip = stack.getTooltipLines(
                Minecraft.getInstance().player,
                Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);

        List<String> lore = new ArrayList<>();

        for (Component text : tooltip) {
            lore.add(text.getString());
        }

        return lore;
    }

    @Override
    public String priceCxn$getItemName() {
        return stack.getItem().getDescriptionId();
    }

    @Override
    public String priceCxn$getDisplayName() {
        return stack.getDisplayName().getString();
    }

    @Override
    public int priceCxn$getCount() {
        return stack.getCount();
    }

    @Override
    public boolean priceCxn$isTrimTemplate() {
        return stack.is(ItemTags.TRIM_TEMPLATES);
    }

    @Override
    public boolean priceCxn$isNetheriteUpgradeSmithingTemplate() {
        return stack.is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
    }

    @Override
    public Optional<String> priceCxn$getRegistryKey() {
        return Optional.of(stack.getItemHolder().kind().name());
    }

    @Override
    public JsonObject priceCxn$getComponentsAsJson() {
        return componentMapToJson(stack.getTag());
    }

    private @NotNull JsonObject componentMapToJson(CompoundTag componentMap) {
        if (componentMap == null || componentMap.isEmpty())
            return new JsonObject();

        JsonObject json = new JsonObject();

        for (String key : componentMap.getAllKeys()) {
            Object component = componentMap.get(key);
            switch (component) {
                case null -> {
                }
                case CompoundTag subComponentMap -> json.add(key, componentMapToJson(subComponentMap));
                case NbtDataComponentContainer subComponentMap -> {
                    try {
                        JsonObject asJsonObject = JsonParser.parseString(subComponentMap.toString()).getAsJsonObject();
                        json.add(key, asJsonObject);
                    } catch (JsonParseException e) {
                        try {
                            JsonArray asJsonObject = JsonParser.parseString(subComponentMap.toString()).getAsJsonArray();
                            json.add(key, asJsonObject);
                        } catch (JsonParseException e1) {
                            json.addProperty(key, subComponentMap.toString());
                        }
                    }
                }
                default -> {
                    Object object = object(component.toString());
                    if (object instanceof JsonElement element)
                        json.add(key, element);
                    else
                        json.addProperty(key, object.toString());
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
}
