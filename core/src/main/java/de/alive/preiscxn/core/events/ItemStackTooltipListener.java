package de.alive.preiscxn.core.events;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.IThemeServerChecker;
import de.alive.preiscxn.api.cytooxien.Modes;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.cytooxien.TranslationDataAccess;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.interfaces.IKeyBinding;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.networking.IServerChecker;
import de.alive.preiscxn.api.utils.TimeUtil;
import de.alive.preiscxn.impl.keybinds.OpenBrowserKeybindExecutor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.serializer.legacy.LegacyComponentSerializer;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.world.ItemStackTooltipEvent;
import org.jetbrains.annotations.NotNull;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ItemStackTooltipListener {
    private final Map<ItemStack, Tuple2<AtomicInteger, PriceCxnItemStack>> itemStacks = new HashMap<>();

    @Subscribe
    public void onItemStackTooltip(ItemStackTooltipEvent event) {
        if (!PriceCxn.getMod().getConnectionManager().isActive()) {
            return;
        }

        List<Component> list = event.getTooltipLines();
        IServerChecker serverChecker = PriceCxn.getMod().getCxnListener().getServerChecker();

        if (shouldCancel(list))
            return;

        Tuple2<AtomicInteger, PriceCxnItemStack> lastUpdatePcxnPriceTuple = itemStacks.get(event.itemStack());
        if (lastUpdatePcxnPriceTuple == null || lastUpdatePcxnPriceTuple.getT1().get() > 50) {
            lastUpdatePcxnPriceTuple = Tuples.of(new AtomicInteger(), getPriceCxnItemStack(event.itemStack()));
            itemStacks.put(event.itemStack(), lastUpdatePcxnPriceTuple);
        }

        AtomicInteger lastUpdate = lastUpdatePcxnPriceTuple.getT1();
        PriceCxnItemStack itemStack = lastUpdatePcxnPriceTuple.getT2();
        lastUpdate.incrementAndGet();

        if (itemStack.getPcxnPrice().isEmpty() && (itemStack.getNookPrice() == null || itemStack.getNookPrice().isEmpty()))
            return;

        AtomicReference<PriceText<?>> pcxnPriceText = new AtomicReference<>(PriceCxn.getMod().createPriceText());

        List<String> lore = new ArrayList<>();
        list.forEach(text -> lore.add(LegacyComponentSerializer.legacySection().serialize(text)));
        int amount = itemStack.getAdvancedAmount(serverChecker, pcxnPriceText, lore);

        list.add((Component) PriceCxn.getMod().space());
        PriceCxnItemStack.ViewMode viewMode = PriceCxn.getMod().getViewMode();

        list.add(
                Component.text("--- ")
                        .color(NamedTextColor.DARK_GRAY)
                        .append((Component) PriceCxn.getMod().getModText())
                        .append(Component.text("x" + (viewMode == PriceCxnItemStack.ViewMode.SINGLE ? 1 : amount))
                                .color(NamedTextColor.DARK_GRAY))
                        .append(Component.text(" ---")
                                .color(NamedTextColor.DARK_GRAY)));

        PriceCxn.getMod().getLogger().debug(String.valueOf(pcxnPriceText.get().getPriceAdder()));
        if (!itemStack.getPcxnPrice().isEmpty()) {
            list.add((Component) pcxnPriceText.get()
                    .withPrices(itemStack
                                    .getPcxnPrice()
                                    .getLowerPrice(),
                            itemStack.getPcxnPrice()
                                    .getUpperPrice())
                    .withPriceMultiplier(PriceCxn.getMod().getViewMode() == PriceCxnItemStack.ViewMode.SINGLE ? 1 : amount)
                    .getText());
        }

        if (itemStack.getNookPrice() != null) {
            list.add((Component) PriceCxn.getMod().createPriceText()
                    .withIdentifierText("Tom Block:")
                    .withPrices(itemStack.getNookPrice().getPrice())
                    .withPriceMultiplier(amount)
                    .getText());

        }
        if (!itemStack.getPcxnPrice().isEmpty()) {

            IKeyBinding keyBinding = PriceCxn.getMod().getKeyBinding(OpenBrowserKeybindExecutor.class);
            if (keyBinding != null && itemStack.getPcxnPrice().has("item_info_url") && !keyBinding.isUnbound()) {
                Component text = Component.translatable("cxn_listener.display_prices.view_in_browser",
                                Component.text(keyBinding.getBoundKeyLocalizedText())
                                        .copy()
                                        .color(NamedTextColor.GOLD))
                        .color(NamedTextColor.GRAY);

                list.add(text);
            }

            list.add((Component) PriceCxn.getMod().space());

            Optional<Tuple2<Long, TimeUtil.TimeUnit>> lastUpdate2
                    = TimeUtil.getTimestampDifference(Long.parseLong(itemStack.getPcxnPrice().getTimestamp()));

            lastUpdate2.ifPresent(s -> {

                Long time = s.getT1();
                String unitTranslatable = s.getT2().getTranslatable(time);

                list.add(Component.translatable("cxn_listener.display_prices.updated",
                                Component.text(time.toString()),
                                Component.translatable(unitTranslatable))
                        .style(((Style) PriceCxn.getMod().getDefaultStyle())));
            });
        }
    }

    private boolean shouldCancel(@NotNull List<Component> list) {
        IThemeServerChecker themeChecker = PriceCxn.getMod().getCxnListener().getThemeChecker();

        Modes mode = themeChecker.getMode();

        if (mode == Modes.NOTHING || mode == Modes.LOBBY)
            return true;

        IMinecraftClient client = PriceCxn.getMod().getMinecraftClient();

        if (client.isPlayerNull() || client.isCurrentScreenNull()) return true;
        if (client.getInventory().getTitle() == null || client.getInventory().getTitle().isEmpty())
            return true;

        List<String> invBlocks = switch (mode) {
            case SKYBLOCK -> TranslationDataAccess.SKYBLOCK_INV_BLOCK.getData().getData();
            case CITYBUILD -> TranslationDataAccess.CITYBUILD_INV_BLOCK.getData().getData();
            default -> null;
        };

        if (invBlocks == null)
            return true;

        String title = client.getInventory().getTitle().toUpperCase();
        for (String s : invBlocks) {
            if (title.contains(s.toUpperCase())) {
                return true;
            }
        }

        for (Component text : list) {
            for (String datum : TranslationDataAccess.VISIT_ISLAND.getData().getData()) {
                if (LegacyComponentSerializer.legacySection().serialize(text).contains(datum)) {
                    return true;
                }
            }
        }

        return false;
    }


    private PriceCxnItemStack getPriceCxnItemStack(ItemStack itemStack) {
        return ((IItemStack) itemStack).priceCxn$createItemStack(null, true, false);
    }
}
