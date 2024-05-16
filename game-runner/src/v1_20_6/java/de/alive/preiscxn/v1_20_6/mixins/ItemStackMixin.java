package de.alive.preiscxn.v1_20_6.mixins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.networking.DataAccess;
import net.labymod.api.component.data.NbtDataComponentContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IItemStack {
    @Unique
    private static final Pattern JSON_KEY_PATTERN = Pattern.compile("([{,])(\\w+):");
    @Unique
    private static final Pattern TO_DELETE_PATTERN = Pattern.compile("[\\\\']");

    @Shadow
    public abstract List<Component> getTooltipLines(Item.TooltipContext $$0, @javax.annotation.Nullable Player $$1, TooltipFlag $$2);

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract Component getDisplayName();

    @Shadow public abstract int getCount();

    @Shadow public abstract boolean is(TagKey<Item> $$0);

    @Shadow public abstract boolean is(Item $$0);

    @Shadow public abstract Holder<Item> getItemHolder();

    @Shadow public abstract DataComponentMap getComponents();

    @Override
    public PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {
        return PriceCxn.getMod().createItemStack(this, searchData, addComment, addTooltips);
    }

    @Override
    public PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData, boolean addComment) {
        return PriceCxn.getMod().createItemStack(this, searchData, addComment);
    }

    @Override
    public PriceCxnItemStack priceCxn$createItemStack(@Nullable Map<String, DataAccess> searchData) {
        return PriceCxn.getMod().createItemStack(this, searchData);
    }

    @Override
    public List<String> priceCxn$getLore() {
        List<Component> tooltip = getTooltipLines(Item.TooltipContext.EMPTY,
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
        return getItem().getDescriptionId();
    }

    @Override
    public String priceCxn$getDisplayName() {
        return getDisplayName().getString();
    }

    @Override
    public int priceCxn$getCount() {
        return getCount();
    }

    @Override
    public boolean priceCxn$isTrimTemplate() {
        return is(ItemTags.TRIM_TEMPLATES);
    }

    @Override
    public boolean priceCxn$isNetheriteUpgradeSmithingTemplate() {
        return is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
    }

    @Override
    public Optional<String> priceCxn$getRegistryKey() {
        return Optional.of(getItemHolder().getRegisteredName());
    }

    @Override
    public JsonObject priceCxn$getComponentsAsJson() {
        return componentMapToJson(getComponents());
    }

    @Unique
    private @NotNull JsonObject componentMapToJson(@NotNull DataComponentMap componentMap) {
        if(componentMap.isEmpty())
            return new JsonObject();

        JsonObject json = new JsonObject();

        for (DataComponentType<?> key : componentMap.keySet()) {
            Object component = componentMap.get(key);
            switch (component) {
                case null -> {
                }
                case DataComponentMap subComponentMap -> json.add(key.toString(), componentMapToJson(subComponentMap));
                case NbtDataComponentContainer subComponentMap -> {
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

    @Unique
    private Object object(String nbtString) {
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
