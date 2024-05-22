package de.alive.preiscxn.v1_20_4.mixins;

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
import net.minecraft.nbt.CompoundTag;
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
    public abstract Item getItem();

    @Shadow
    public abstract Component getDisplayName();

    @Shadow
    public abstract int getCount();

    @Shadow
    public abstract boolean is(TagKey<Item> $$0);

    @Shadow
    public abstract boolean is(Item $$0);

    @Shadow
    public abstract Holder<Item> getItemHolder();


    @Shadow
    public abstract List<Component> getTooltipLines(@Nullable Player $$0, TooltipFlag $$1);

    @Shadow
    @javax.annotation.Nullable
    public abstract CompoundTag getTag();

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
        List<Component> tooltip = getTooltipLines(
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
        return Optional.of(getItemHolder().kind().name());
    }

    @Override
    public JsonObject priceCxn$getComponentsAsJson() {
        return componentMapToJson(getTag());
    }

    @Unique
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
}
