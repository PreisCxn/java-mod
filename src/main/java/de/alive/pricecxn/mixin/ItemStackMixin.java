package de.alive.pricecxn.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.PriceCxnModClient;
import de.alive.pricecxn.cytooxien.*;
import de.alive.pricecxn.keybinds.KeybindExecutor;
import de.alive.pricecxn.keybinds.OpenBrowserKeybindExecutor;
import de.alive.pricecxn.networking.DataHandler;
import de.alive.pricecxn.networking.ServerChecker;
import de.alive.pricecxn.utils.StringUtil;
import de.alive.pricecxn.utils.TimeUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.alive.pricecxn.PriceCxnMod.LOGGER;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract void removeCustomName();

    @Shadow
    public abstract Item getItem();

    @Shadow
    private int count;
    @Unique
    private @Nullable JsonObject nookPrice = null;
    @Unique
    private @Nullable JsonObject pcxnPrice = null;
    @Unique
    private long lastUpdate = 0;
    @Unique
    private final StorageItemStack storageItemStack = new StorageItemStack();
    @Unique
    private @NotNull String searchingString = "";
    @Unique
    private int searchingCount = 20;
    @Unique
    private @Nullable PriceCxnItemStack cxnItemStack = null;

    @Inject(method = "getTooltip", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void getToolTip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> callbackInfoReturnable, @NotNull List<Text> list) {

        ServerChecker serverChecker = PriceCxnModClient.CXN_LISTENER.getServerChecker();
        ThemeServerChecker themeChecker = PriceCxnModClient.CXN_LISTENER.getThemeChecker();

        if(shouldCancel(themeChecker))
            return;

        findInfo();

        if ((pcxnPrice == null || pcxnPrice.isEmpty()) && (nookPrice == null || nookPrice.isEmpty())) return;

        if (this.cxnItemStack == null) return;

        int amount = this.cxnItemStack.getAmount();

        PriceText pcxnPriceText = PriceText.create();


        if (pcxnPrice != null && pcxnPrice.has("pbv_search_key") && pcxnPrice.get("pbv_search_key") != JsonNull.INSTANCE) {
            String pbvKey = pcxnPrice.get("pbv_search_key").getAsString();
            if (!this.cxnItemStack.getDataWithoutDisplay().has(PriceCxnItemStack.COMMENT_KEY)) return;
            JsonObject nbtData = this.cxnItemStack.getDataWithoutDisplay().get(PriceCxnItemStack.COMMENT_KEY).getAsJsonObject();
            if (!nbtData.has("PublicBukkitValues")) return;
            JsonObject pbvData = nbtData.get("PublicBukkitValues").getAsJsonObject();
            if (!pbvData.has(pbvKey)) return;

            String pbvSearchResult = StringUtil.removeChars(pbvData.get(pbvKey).getAsString());

            int pbvAmount = 0;

            try {
                pbvAmount = Integer.parseInt(pbvSearchResult);
            } catch (NumberFormatException e) {
                LOGGER.error("fehler beim konvertieren des pbv Daten im Item: ", e);
                return;
            }

            if (StorageItemStack.isOf(pcxnPrice)) {

                storageItemStack.setup(pcxnPrice, serverChecker.getWebsocket());
                pcxnPriceText = storageItemStack.getText();
                storageItemStack.search(pbvAmount).block();

            } else {

                amount *= pbvAmount;

            }
        }

        list.add(PriceText.space());

        list.add(
                MutableText.of(new PlainTextContent.Literal("--- "))
                        .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))
                        .append(PriceCxnMod.MOD_TEXT.copy())
                        .append(MutableText.of(new PlainTextContent.Literal("---"))
                                .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))));

        int finalAmount = amount;
        PriceText finalPcxnPriceText = pcxnPriceText;
        LOGGER.debug(String.valueOf(finalPcxnPriceText.getPriceAdder()));
        if(pcxnPrice != null){
            list.add(finalPcxnPriceText
                             .withPrices(pcxnPrice.get("lower_price").getAsDouble(), pcxnPrice.get("upper_price").getAsDouble())
                             .withPriceMultiplier(finalAmount)
                             .getText());
        }

        if(nookPrice != null){
            list.add(PriceText.create()
                             .withIdentifierText("Tom Block:")
                             .withPrices(nookPrice.get("price").getAsDouble())
                             .withPriceMultiplier(finalAmount)
                             .getText());

        }
        if(pcxnPrice != null){
            if(pcxnPrice.has("item_info_url")){
                KeyBinding keyBinding = KeybindExecutor.CLASS_KEY_BINDING_MAP.get(OpenBrowserKeybindExecutor.class);
                MutableText text = keyBinding.getBoundKeyLocalizedText().copy()
                        .setStyle(Style.EMPTY.withColor(Formatting.GRAY))
                        .append(Text.translatable("cxn_listener.display_prices.separator")
                                        .setStyle(Style.EMPTY.withColor(Formatting.DARK_RED)))
                        .append(Text.translatable("cxn_listener.display_prices.view_in_browser"));

                list.add(text);
            }

            list.add(PriceText.space());

            Optional<Pair<Long, TimeUtil.TimeUnit>> lastUpdate
                    = TimeUtil.getTimestampDifference(Long.parseLong(pcxnPrice.get("timestamp").getAsString()));

            lastUpdate.ifPresent(s -> {

                Long time = s.getLeft();
                String unitTranslatable = s.getRight().getTranslatable(time);

                list.add(Text.translatable("cxn_listener.display_prices.updated", time.toString(), Text.translatable(unitTranslatable))
                                 .setStyle(PriceCxnMod.DEFAULT_TEXT.withFormatting(Formatting.ITALIC)));
            });
        }
    }

    @Unique
    public boolean shouldCancel(ThemeServerChecker themeChecker){
        Modes mode = themeChecker.getMode();

        if (mode == Modes.NOTHING || mode == Modes.LOBBY)
            return true;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.currentScreen == null) return true;
        if (client.currentScreen.getTitle().getString() == null || client.currentScreen.getTitle().getString().isEmpty())
            return true;

        List<String> invBlocks = switch(mode){
            case SKYBLOCK -> TranslationDataAccess.SKYBLOCK_INV_BLOCK.getData();
            case CITYBUILD -> TranslationDataAccess.CITYBUILD_INV_BLOCK.getData();
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

        return false;
    }

    @Unique
    private void findInfo() {
        if (this.lastUpdate + DataHandler.ITEM_REFRESH_INTERVAL > System.currentTimeMillis()) return;

        ItemStack itemStack = (ItemStack) (Object) this;
        this.cxnItemStack = new PriceCxnItemStack(itemStack, null, true, false);

        this.pcxnPrice = cxnItemStack.findItemInfo("pricecxn.data.item_data");
        this.nookPrice = cxnItemStack.findItemInfo("pricecxn.data.nook_data");

    }
}
