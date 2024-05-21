package de.alive.preiscxn.fabric.v1_20_5.mixin;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.IThemeServerChecker;
import de.alive.preiscxn.api.cytooxien.Modes;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.cytooxien.TranslationDataAccess;
import de.alive.preiscxn.api.interfaces.IKeyBinding;
import de.alive.preiscxn.api.networking.IServerChecker;
import de.alive.preiscxn.api.utils.TimeUtil;
import de.alive.preiscxn.fabric.v1_20_5.impl.ItemStackImpl;
import de.alive.preiscxn.impl.cytooxien.PriceCxnItemStackImpl;
import de.alive.preiscxn.impl.keybinds.OpenBrowserKeybindExecutor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract boolean isEmpty();

    @Unique
    private @Nullable PriceCxnItemStackImpl cxnItemStack = null;

    @Unique
    private long lastUpdate = 0;

    @Inject(method = "getTooltip", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void getToolTip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (!PriceCxn.getMod().getConnectionManager().isActive()) {
            return;
        }

        List<Text> list = cir.getReturnValue();
        IServerChecker serverChecker = PriceCxn.getMod().getCxnListener().getServerChecker();

        if (shouldCancel(list))
            return;

        if (this.cxnItemStack == null || this.lastUpdate > 50) {
            this.cxnItemStack = getPriceCxnItemStack();
            this.lastUpdate = 0;
        }

        this.lastUpdate++;
        if ((this.cxnItemStack.getPcxnPrice() == null || this.cxnItemStack.getPcxnPrice().isEmpty())
            && (this.cxnItemStack.getNookPrice() == null || this.cxnItemStack.getNookPrice().isEmpty()))
            return;

        AtomicReference<PriceText<?>> pcxnPriceText = new AtomicReference<>(PriceCxn.getMod().createPriceText());

        List<String> lore = new ArrayList<>();
        list.forEach(text -> lore.add(text.getString()));
        int amount = this.cxnItemStack.getAdvancedAmount(serverChecker, pcxnPriceText, lore);

        list.add((Text) PriceCxn.getMod().space());
        PriceCxnItemStack.ViewMode viewMode = PriceCxn.getMod().getViewMode();

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
        if (this.cxnItemStack.getPcxnPrice() != null) {
            list.add((Text) pcxnPriceText.get()
                    .withPrices(this.cxnItemStack
                            .getPcxnPrice()
                            .get("lower_price")
                            .getAsDouble(),
                            this.cxnItemStack.getPcxnPrice()
                                    .get("upper_price")
                                    .getAsDouble())
                    .withPriceMultiplier(PriceCxn.getMod().getViewMode() == PriceCxnItemStack.ViewMode.SINGLE ? 1 : amount)
                    .getText());
        }

        if (this.cxnItemStack.getNookPrice() != null) {
            list.add((Text) PriceCxn.getMod().createPriceText()
                             .withIdentifierText("Tom Block:")
                             .withPrices(this.cxnItemStack.getNookPrice().get("price").getAsDouble())
                             .withPriceMultiplier(amount)
                             .getText());

        }
        if (this.cxnItemStack.getPcxnPrice() != null) {

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
                    = TimeUtil.getTimestampDifference(Long.parseLong(this.cxnItemStack.getPcxnPrice().get("timestamp").getAsString()));

            lastUpdate.ifPresent(s -> {

                Long time = s.getT1();
                String unitTranslatable = s.getT2().getTranslatable(time);

                list.add(Text.translatable("cxn_listener.display_prices.updated", time.toString(), Text.translatable(unitTranslatable))
                        .setStyle(((Style) PriceCxn.getMod().getDefaultStyle()).withFormatting()));
            });
        }
    }

    @Unique
    public boolean shouldCancel(@NotNull List<Text> list) {
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
    private PriceCxnItemStackImpl getPriceCxnItemStack() {
        return PriceCxnItemStackImpl.getInstance(new ItemStackImpl((ItemStack) (Object) this), null, true, false);
    }
}
