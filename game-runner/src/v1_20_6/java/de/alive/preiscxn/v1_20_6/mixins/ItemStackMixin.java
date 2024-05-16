package de.alive.preiscxn.v1_20_6.mixins;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.IThemeServerChecker;
import de.alive.preiscxn.api.cytooxien.Modes;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.cytooxien.TranslationDataAccess;
import de.alive.preiscxn.api.interfaces.IKeyBinding;
import de.alive.preiscxn.api.networking.IServerChecker;
import de.alive.preiscxn.api.utils.TimeUtil;
import de.alive.preiscxn.impl.cytooxien.PriceCxnItemStackImpl;
import de.alive.preiscxn.impl.keybinds.OpenBrowserKeybindExecutor;
import de.alive.preiscxn.v1_20_6.impl.ItemStackImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
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

    @Unique
    private @Nullable PriceCxnItemStackImpl cxnItemStack = null;

    @Unique
    private long lastUpdate = 0;

    @Inject(method = "getTooltipLines", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void getToolTip(CallbackInfoReturnable<List<Component>> cir) {
        if (!PriceCxn.getMod().getConnectionManager().isActive()) {
            return;
        }

        List<Component> list = cir.getReturnValue();
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

        list.add((Component) PriceCxn.getMod().space());
        PriceCxnItemStack.ViewMode viewMode = PriceCxn.getMod().getViewMode();

        list.add(
                Component.literal("--- ")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY))
                        .append(((Component) PriceCxn.getMod().getModText()).copy())
                        .append(Component.literal("x" + (viewMode == PriceCxnItemStack.ViewMode.SINGLE ? 1 : amount))
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)))
                        .append(Component.literal(" ---")
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY))));

        PriceCxn.getMod().getLogger().debug(String.valueOf(pcxnPriceText.get().getPriceAdder()));
        if (this.cxnItemStack.getPcxnPrice() != null) {
            list.add((Component) pcxnPriceText.get()
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
            list.add((Component) PriceCxn.getMod().createPriceText()
                             .withIdentifierText("Tom Block:")
                             .withPrices(this.cxnItemStack.getNookPrice().get("price").getAsDouble())
                             .withPriceMultiplier(amount)
                             .getText());

        }
        if (this.cxnItemStack.getPcxnPrice() != null) {

            IKeyBinding keyBinding = PriceCxn.getMod().getKeyBinding(OpenBrowserKeybindExecutor.class);
            if (this.cxnItemStack.getPcxnPrice().has("item_info_url") && !keyBinding.isUnbound()) {
                Component text = Component.translatable("cxn_listener.display_prices.view_in_browser",
                                Component.keybind(keyBinding.getBoundKeyLocalizedText())
                                              .copy()
                                              .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));

                list.add(text);
            }

            list.add((Component) PriceCxn.getMod().space());

            Optional<Tuple2<Long, TimeUtil.TimeUnit>> lastUpdate
                    = TimeUtil.getTimestampDifference(Long.parseLong(this.cxnItemStack.getPcxnPrice().get("timestamp").getAsString()));

            lastUpdate.ifPresent(s -> {

                Long time = s.getT1();
                String unitTranslatable = s.getT2().getTranslatable(time);

                list.add(Component.translatable("cxn_listener.display_prices.updated", time.toString(), Component.translatable(unitTranslatable))
                        .setStyle(((Style) PriceCxn.getMod().getDefaultStyle())));
            });
        }
    }

    @Unique
    public boolean shouldCancel(@NotNull List<Component> list) {
        IThemeServerChecker themeChecker = PriceCxn.getMod().getCxnListener().getThemeChecker();

        Modes mode = themeChecker.getMode();

        if (mode == Modes.NOTHING || mode == Modes.LOBBY)
            return true;

        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.screen == null) return true;
        if (client.screen.getTitle().getString().isEmpty())
            return true;

        List<String> invBlocks = switch (mode) {
            case SKYBLOCK -> TranslationDataAccess.SKYBLOCK_INV_BLOCK.getData().getData();
            case CITYBUILD -> TranslationDataAccess.CITYBUILD_INV_BLOCK.getData().getData();
            default -> null;
        };

        if (invBlocks == null)
            return true;

        String title = client.screen.getTitle().getString().toUpperCase();
        for (String s : invBlocks) {
            if (title.contains(s.toUpperCase())) {
                return true;
            }
        }

        for (Component text : list) {
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
        return PriceCxnItemStackImpl.getInstance(new ItemStackImpl().setStack((ItemStack) (Object) this), null, true, false);
    }
}
