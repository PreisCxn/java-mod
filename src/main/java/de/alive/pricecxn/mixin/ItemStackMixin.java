package de.alive.pricecxn.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.PriceCxnModClient;
import de.alive.pricecxn.cytooxien.*;
import de.alive.pricecxn.networking.DataHandler;
import de.alive.pricecxn.networking.ServerChecker;
import de.alive.pricecxn.utils.StringUtil;
import de.alive.pricecxn.utils.TimeUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
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
    private JsonObject nookPrice = null;
    @Unique
    private JsonObject pcxnPrice = null;
    @Unique
    private long lastUpdate = 0;
    @Unique
    private final StorageItemStack storageItemStack = new StorageItemStack();
    @Unique
    private String searchingString = "";
    @Unique
    private int searchingCount = 20;
    @Unique
    private PriceCxnItemStack cxnItemStack = null;

    @Inject(method = "getTooltip", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void getToolTip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> callbackInfoReturnable, List<Text> list) {

        ServerChecker serverChecker = PriceCxnModClient.CXN_LISTENER.getServerChecker();
        ThemeServerChecker themeChecker = PriceCxnModClient.CXN_LISTENER.getThemeChecker();

        ItemStack itemStack = (ItemStack) (Object) this;

        if (serverChecker == null) return;
        if (themeChecker == null) return;

        Modes mode = themeChecker.getMode();

        if (mode == Modes.NOTHING || mode == Modes.LOBBY) return;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.currentScreen == null) return;
        if (client.currentScreen.getTitle().getString() == null || client.currentScreen.getTitle().getString().isEmpty())
            return;

        String title = client.currentScreen.getTitle().getString().toUpperCase();

        List<String> invBlocks = mode == Modes.SKYBLOCK ?
                TranslationDataAccess.SKYBLOCK_INV_BLOCK.getData() :
                mode == Modes.CITYBUILD ?
                        TranslationDataAccess.CITYBUILD_INV_BLOCK.getData() :
                        null;

        if (invBlocks == null) return;

        for(String s : invBlocks) {
            if(title.contains(s.toUpperCase())) {
                return;
            }
        }

        findInfo();

        if (pcxnPrice.isEmpty() && nookPrice.isEmpty()) return;

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
                System.err.println("fehler beim konvertieren des pbv Daten im Item: " + e);
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
        System.out.println(finalPcxnPriceText.getPriceAdder());
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
    private void findInfo() {
        if (this.lastUpdate + DataHandler.ITEM_REFRESH_INTERVAL > System.currentTimeMillis()) return;

        ItemStack itemStack = (ItemStack) (Object) this;
        this.cxnItemStack = new PriceCxnItemStack(itemStack, null, true, false);

        pcxnPrice = findItemInfo("pricecxn.data.item_data", cxnItemStack);
        nookPrice = findItemInfo("pricecxn.data.nook_data", cxnItemStack);

    }

    @Unique
    private JsonObject findItemInfo(String dataKey, PriceCxnItemStack cxnItemStack) {
        JsonObject cxnItemData = cxnItemStack.getDataWithoutDisplay();
        if (cxnItemData == null) return null;

        JsonObject obj = PriceCxnModClient.CXN_LISTENER.getData(dataKey).getDataObject();
        ThemeServerChecker themeChecker = PriceCxnModClient.CXN_LISTENER.getThemeChecker();

        if (obj == null) return null;

        if (!obj.has("mode") || !obj.has("is_nook") || !obj.has("data") || !obj.has("is_mod")) return null;
        if (!obj.get("is_mod").getAsBoolean()) return null;
        if (!obj.get("mode").getAsString().equals(themeChecker.getMode().getTranslationKey())) return null;

        JsonArray array = obj.get("data").getAsJsonArray();
        if (array.isEmpty()) return null;

        this.lastUpdate = System.currentTimeMillis();

        List<Integer> foundItems = new ArrayList<>();

        //item ist special_item?
        if (cxnItemData.has(PriceCxnItemStack.COMMENT_KEY) &&
                cxnItemData.get(PriceCxnItemStack.COMMENT_KEY).isJsonObject() &&
                cxnItemData.get(PriceCxnItemStack.COMMENT_KEY).getAsJsonObject().has("PublicBukkitValues")) {
            JsonObject nbtData = cxnItemData.get(PriceCxnItemStack.COMMENT_KEY).getAsJsonObject();
            String pbvString = nbtData.get("PublicBukkitValues").getAsJsonObject().toString();

            outer:
            for (int i = 0; i < array.size(); i++) {
                JsonObject item = array.get(i).getAsJsonObject();
                if (!item.has("item_search_key") || !item.get("item_search_key").getAsString().contains("special_item"))
                    continue;

                String searchKey = item.get("item_search_key").getAsString();
                String[] searches = searchKey.split("\\.");

                for (String s : searches) {
                    if (!pbvString.contains(s)) continue outer;
                }

                foundItems.add(i);
                if (foundItems.size() > 1) return null;

            }

        } else {
            String itemName = cxnItemStack.getItemName();

            outer:
            for (int i = 0; i < array.size(); i++) {
                JsonObject item = array.get(i).getAsJsonObject();
                if (!item.has("item_search_key") || item.get("item_search_key").getAsString().contains("special_item"))
                    continue;

                String searchKey = item.get("item_search_key").getAsString();
                String[] searches = searchKey.split("&c>");
                String itemNameSearch = searches[0];

                if (!itemName.equals(itemNameSearch)) continue;

                if (searches.length > 1) {
                    String[] nbtSearches = searches[1].split("\\.");
                    String commentSearch = cxnItemData.get(PriceCxnItemStack.COMMENT_KEY).getAsJsonObject().toString();

                    for (String s : nbtSearches) {
                        if (!commentSearch.contains(s)) continue outer;
                    }

                }

                foundItems.add(i);
                if (foundItems.size() > 1) return null;

            }

        }

        if (foundItems.size() == 1) {
            return array.get(foundItems.get(0)).getAsJsonObject();
        }

        return null;
    }

}
