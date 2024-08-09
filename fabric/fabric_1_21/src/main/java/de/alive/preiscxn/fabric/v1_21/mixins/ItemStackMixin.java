package de.alive.preiscxn.fabric.v1_21.mixins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.IThemeServerChecker;
import de.alive.preiscxn.api.cytooxien.Modes;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.cytooxien.TranslationDataAccess;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.interfaces.IKeyBinding;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.api.networking.IServerChecker;
import de.alive.preiscxn.api.utils.TimeUtil;
import de.alive.preiscxn.impl.keybinds.OpenBrowserKeybindExecutor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IItemStack {
    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract Text getName();

    @Shadow
    public abstract int getCount();

    @Shadow
    public abstract boolean isIn(TagKey<Item> tag);

    @Shadow
    public abstract String getTranslationKey();

    @Shadow
    public abstract boolean isOf(Item item);

    @Shadow
    public abstract RegistryEntry<Item> getRegistryEntry();

    @Shadow
    public abstract ComponentMap getComponents();

    @Shadow
    public abstract Item getItem();

    @Unique
    private PriceCxnItemStack cxnItemStack = null;

    @Unique
    private long lastUpdate = 0;

    @Inject(method = "getTooltip", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void getToolTip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (cir == null)
            return;

        if (!PriceCxn.getMod().getConnectionManager().isActive()) {
            return;
        }

        List<Text> list = new ArrayList<>();
        IServerChecker serverChecker = PriceCxn.getMod().getCxnListener().getServerChecker();

        if (shouldCancel(cir.getReturnValue()))
            return;

        if (this.cxnItemStack == null || this.lastUpdate > 50) {
            this.cxnItemStack = getPriceCxnItemStack();
            this.lastUpdate = 0;
        }

        this.lastUpdate++;
        if (this.cxnItemStack.getPcxnPrice().isEmpty() && this.cxnItemStack.getNookPrice().isEmpty())
            return;

        AtomicReference<PriceText<?>> pcxnPriceText = new AtomicReference<>(PriceCxn.getMod().createPriceText());

        List<String> lore = new ArrayList<>();
        cir.getReturnValue().forEach(text -> lore.add(text.getString()));
        int amount = this.cxnItemStack.getAdvancedAmount(serverChecker, pcxnPriceText, lore);



        list.add((Text) PriceCxn.getMod().space());
        PriceCxnItemStack.ViewMode viewMode = PriceCxn.getMod().getViewMode();

        boolean addedData = false;
        list.add(
                MutableText.of(new PlainTextContent.Literal("--- "))
                        .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))
                        .append(((Text) PriceCxn.getMod().getModText()).copy())
                        .append(MutableText.of(new PlainTextContent.Literal("x" + (viewMode == PriceCxnItemStack.ViewMode.SINGLE ? 1 : amount)))
                                .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))
                        )
                        .append(MutableText.of(new PlainTextContent.Literal(" ---"))
                                .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))));

        PriceCxn.getMod().getLogger().debug(String.valueOf(pcxnPriceText.get().getPriceAdder()));
        if (!this.cxnItemStack.getPcxnPrice().isEmpty()) {
            addedData = true;
            list.add((Text) pcxnPriceText.get()
                    .withPrices(this.cxnItemStack
                            .getPcxnPrice()
                            .getLowerPrice(),
                            this.cxnItemStack.getPcxnPrice()
                                    .getUpperPrice())
                    .withPriceMultiplier(PriceCxn.getMod().getViewMode() == PriceCxnItemStack.ViewMode.SINGLE ? 1 : amount)
                    .getText());
        }

        if (!this.cxnItemStack.getNookPrice().isEmpty()) {
            addedData = true;
            list.add((Text) PriceCxn.getMod().createPriceText()
                             .withIdentifierText("Tom Block:")
                             .withPrices(this.cxnItemStack.getNookPrice().getPrice())
                             .withPriceMultiplier(amount)
                             .getText());

        }
        if (!this.cxnItemStack.getPcxnPrice().isEmpty()) {
            addedData = true;
            IKeyBinding keyBinding = PriceCxn.getMod().getKeyBinding(OpenBrowserKeybindExecutor.class);
            if (this.cxnItemStack.getPcxnPrice().has("item_info_url") && !keyBinding.isUnbound()) {
                MutableText text = Text.translatable("cxn_listener.display_prices.view_in_browser",
                                      Text.of(keyBinding.getBoundKeyLocalizedText())
                                              .copy()
                                              .setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
                        .setStyle(Style.EMPTY.withColor(Formatting.GRAY));

                list.add(text);
            }

            list.add((Text) PriceCxn.getMod().space());

            Optional<Tuple2<Long, TimeUtil.TimeUnit>> lastUpdate
                    = TimeUtil.getTimestampDifference(Long.parseLong(this.cxnItemStack.getPcxnPrice().getTimestamp()));

            lastUpdate.ifPresent(s -> {

                Long time = s.getT1();
                String unitTranslatable = s.getT2().getTranslatable(time);

                list.add(Text.translatable("cxn_listener.display_prices.updated", time.toString(), Text.translatable(unitTranslatable))
                        .setStyle(((Style) PriceCxn.getMod().getDefaultStyle()).withFormatting()));
            });
        }

        if (addedData) {
            cir.getReturnValue().addAll(list);
        }
    }

    @Unique
    private boolean shouldCancel(@NotNull List<Text> list) {
        IThemeServerChecker themeChecker = PriceCxn.getMod().getCxnListener().getThemeChecker();

        Modes mode = themeChecker.getMode();

        if (mode == Modes.NOTHING || mode == Modes.LOBBY)
            return true;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.currentScreen == null) return true;
        if (client.currentScreen.getTitle().getString() == null || client.currentScreen.getTitle().getString().isEmpty())
            return true;

        List<String> invBlocks = switch (mode) {
            case SKYBLOCK -> TranslationDataAccess.SKYBLOCK_INV_BLOCK.getData().getData();
            case CITYBUILD -> TranslationDataAccess.CITYBUILD_INV_BLOCK.getData().getData();
            default -> null;
        };

        if (invBlocks == null)
            return true;

        String title = client.currentScreen.getTitle().getString().toUpperCase();
        for (String s : invBlocks) {
            if (title.contains(s.toUpperCase())) {
                return true;
            }
        }

        for (Text text : list) {
            for (String datum : TranslationDataAccess.VISIT_ISLAND.getData().getData()) {
                if (text.getString().contains(datum)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Unique
    private PriceCxnItemStack getPriceCxnItemStack() {
        return priceCxn$createItemStack(null, true, false);
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
        List<Text> tooltip = ((ItemStack) (Object) this).getTooltip(Item.TooltipContext.DEFAULT,
                MinecraftClient.getInstance().player,
                MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC);

        List<String> lore = new ArrayList<>();

        for (Text text : tooltip) {
            lore.add(text.getString());
        }

        return lore;
    }

    @Override
    public String priceCxn$getItemName() {
        return getTranslationKey();
    }

    @Override
    public String priceCxn$getDisplayName() {
        return getName().getString();
    }

    @Override
    public int priceCxn$getCount() {
        return getCount();
    }

    @Override
    public boolean priceCxn$isTrimTemplate() {
        return isIn(ItemTags.TRIM_TEMPLATES);
    }

    @Override
    public boolean priceCxn$isNetheriteUpgradeSmithingTemplate() {
        return isOf(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
    }

    @Override
    public Optional<String> priceCxn$getRegistryKey() {
        return getRegistryEntry().getKey()
                .map(itemRegistryKey -> itemRegistryKey.getValue().getPath());
    }

    @Override
    public JsonObject priceCxn$getComponentsAsJson() {
        return componentMapToJson(getComponents());
    }

    @Unique
    private @NotNull JsonObject componentMapToJson(@NotNull ComponentMap componentMap) {
        if (componentMap.isEmpty())
            return new JsonObject();

        JsonObject json = new JsonObject();

        for (ComponentType<?> key : componentMap.getTypes()) {
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
}
