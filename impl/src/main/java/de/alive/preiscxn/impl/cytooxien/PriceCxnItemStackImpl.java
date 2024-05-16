package de.alive.preiscxn.impl.cytooxien;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.IThemeServerChecker;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.cytooxien.PriceText;
import de.alive.preiscxn.api.cytooxien.TranslationDataAccess;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.networking.DataAccess;
import de.alive.preiscxn.api.networking.IServerChecker;
import de.alive.preiscxn.api.utils.StringUtil;
import org.checkerframework.common.aliasing.qual.Unique;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;


public final class PriceCxnItemStackImpl implements PriceCxnItemStack {

    private static final Cache<Tuple4<IItemStack, Map<String, DataAccess>, Boolean, Boolean>, PriceCxnItemStackImpl> CACHE
            = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .build();
    private final @NotNull Map<String, DataAccess> searchData;

    private final JsonObject data = new JsonObject();
    private final int amount;
    private final StorageItemStack storageItemStack = new StorageItemStack();
    private String itemName;
    private List<String> toolTips;
    private @Nullable JsonObject nookPrice = null;
    private @Nullable JsonObject pcxnPrice = null;

    private PriceCxnItemStackImpl(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {

        this.searchData = Objects.requireNonNullElseGet(searchData, HashMap::new);

        if (addTooltips)
            this.toolTips = StringUtil.getToolTips(item);
        this.itemName = item.priceCxn$getItemName();
        String displayName = item.priceCxn$getDisplayName();
        this.amount = item.priceCxn$getCount();

        if (item.priceCxn$isTrimTemplate() || item.priceCxn$isNetheriteUpgradeSmithingTemplate()) {

            item.priceCxn$getRegistryKey()
                    .ifPresent(s -> this.itemName += "." + s);
        }

        /*
        wird immer gesucht:
        - itemName
        - amount
        - display name + current lang
        - comment (nbts)
         */
        data.addProperty(ITEM_NAME_KEY, itemName);
        data.addProperty(AMOUNT_KEY, amount);
        data.addProperty(DISPLAY_NAME_KEY, displayName);
        data.addProperty(MC_CLIENT_LANG_KEY, PriceCxn.getMod().getMinecraftClient().getLanguage());
        if (addComment)
            data.add(COMMENT_KEY, getCustomData(item));

        PriceCxn.getMod().getLogger().debug(itemName);

        /*
        zusätzlich suche nach den keys in searchData:
         */

        for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {

            DataAccess access = entry.getValue();
            JsonElement result = access.getData().getDefaultResult();
            String searchResult = this.toolTipSearch(access);

            if (searchResult != null) {
                if (entry.getValue().getData().hasProcessData() && access.getData().getProcessData() != null) {
                    result = access.getData().getProcessData().apply(new JsonPrimitive(searchResult));
                } else {
                    result = new JsonPrimitive(searchResult);
                }
            }

            data.add(entry.getKey(), result);
        }

        this.pcxnPrice = findItemInfo("pricecxn.data.item_data");
        this.nookPrice = findItemInfo("pricecxn.data.nook_data");
    }

    public static PriceCxnItemStackImpl getInstance(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment) {
        return getInstance(item, searchData, addComment, true);
    }

    public static PriceCxnItemStackImpl getInstance(@NotNull IItemStack item, @Nullable Map<String, DataAccess> searchData) {
        return getInstance(item, searchData, true, true);
    }

    public static PriceCxnItemStackImpl getInstance(@NotNull IItemStack item,
                                                    @Nullable Map<String, DataAccess> searchData,
                                                    boolean addComment,
                                                    boolean addTooltips) {
        try {
            return CACHE
                    .get(Tuples.of(item,
                                    searchData == null ? Collections.emptyMap() : searchData,
                                    addComment,
                                    addTooltips),
                            () -> new PriceCxnItemStackImpl(item,
                                    searchData,
                                    addComment,
                                    addTooltips));
        } catch (ExecutionException e) {
            return new PriceCxnItemStackImpl(item, searchData, addComment, addTooltips);
        }
    }

    private @NotNull JsonObject getCustomData(@NotNull IItemStack item) {
        JsonObject jsonObject = item.priceCxn$getComponentsAsJson();
        if (jsonObject == null) return new JsonObject();

        if (jsonObject.get("minecraft:custom_data") instanceof JsonPrimitive) {
            PriceCxn.getMod().getLogger().warn("Found no custom_data in item: " + item.priceCxn$getItemName());
            return new JsonObject();
        }
        return jsonObject.getAsJsonObject("minecraft:custom_data");
    }

    private @Nullable String toolTipSearch(@NotNull DataAccess access) {
        String result;
        for (String prefix : access.getData().getData()) {
            result = StringUtil.getFirstSuffixStartingWith(this.toolTips, prefix);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public int hashCode() {
        JsonObject hash = this.data.deepCopy();

        for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {
            if (entry.getValue().getData().hasEqualData()) {
                hash.remove(entry.getKey());
            }
        }

        return hash.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof PriceCxnItemStackImpl
                && ((PriceCxnItemStackImpl) o).getEqualData().equals(this.getEqualData());
    }

    @Override
    public boolean deepEquals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PriceCxnItemStackImpl item)) return false;

        for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {
            if (entry.getValue().getData().hasEqualData()) {

                JsonElement el1 = item.getData().get(entry.getKey());
                JsonElement el2 = this.getData().get(entry.getKey());

                if (!Objects.requireNonNull(entry.getValue().getData().getEqualData()).apply(Tuples.of(el1, el2)))
                    return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return this.data.toString();
    }

    @Override
    public @NotNull JsonObject getData() {
        return data;
    }

    @Override
    public @NotNull JsonObject getDataWithoutDisplay() {
        JsonObject data = this.data.deepCopy();
        data.get(COMMENT_KEY).getAsJsonObject().remove("display");
        return data;
    }

    private @NotNull JsonObject getEqualData() {
        JsonObject hash = this.data.deepCopy();

        if (hash.has(COMMENT_KEY) && !hash.get(COMMENT_KEY).isJsonObject())
            hash.add(COMMENT_KEY, new JsonObject());

        if (hash.has(COMMENT_KEY) && hash.getAsJsonObject(COMMENT_KEY).has("display"))
            hash.get(COMMENT_KEY).getAsJsonObject().remove("display");

        for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {
            if (entry.getValue().getData().hasEqualData()) {
                hash.remove(entry.getKey());
            }
        }

        return hash;
    }

    @Override
    public boolean isSameItem(@NotNull PriceCxnItemStack item) {
        if (!Objects.equals(this.getItemName(), item.getItemName()))
            return false; //wenn itemName nicht gleich => false

        //itemName ist gleich

        if (!(this.getData().has(COMMENT_KEY) && item.getData().has(COMMENT_KEY)))
            return true; //wenn beide keinen Comment => true
        if (this.getData().has(COMMENT_KEY) != item.getData().has(COMMENT_KEY))
            return false; //wenn nur einer einen Comment => false

        //beide haben einen Comment

        JsonObject thisComment = this.getData().get(COMMENT_KEY).getAsJsonObject();
        JsonObject itemComment = item.getData().get(COMMENT_KEY).getAsJsonObject();

        final String bukkitValue = "PublicBukkitValues";

        if (!(thisComment.has(bukkitValue) && itemComment.has(bukkitValue)))
            return true; //wenn beide keinen PublicBukkitValues haben => true
        if (thisComment.has(bukkitValue) != itemComment.has(bukkitValue))
            return false; //wenn nur einer einen PublicBukkitValues hat => false

        //beide haben PublicBukkitValues

        return thisComment.get(bukkitValue).equals(itemComment.get(bukkitValue)); //wenn beide PublicBukkitValues gleich => true
    }

    @Override
    public String getItemName() {
        return itemName;
    }

    @Override
    public void updateData(@NotNull PriceCxnItemStack item) {
        for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {
            if (entry.getValue().getData().hasEqualData()) {
                if (!item.getSearchData().containsKey(entry.getKey())) continue;
                if (!item.getSearchData().get(entry.getKey()).getData().hasEqualData()) continue;

                this.data.remove(entry.getKey());
                this.data.add(entry.getKey(), item.getData().get(entry.getKey()));
            }
        }
    }

    @Override
    public int getAmount() {
        return amount;
    }

    public @Nullable JsonObject getPcxnPrice() {
        return pcxnPrice;
    }

    public @Nullable JsonObject getNookPrice() {
        return nookPrice;
    }

    @Override
    public int getAdvancedAmount(@NotNull IServerChecker serverChecker,
                                 @Nullable AtomicReference<PriceText<?>> pcxnPriceText,
                                 @Nullable List<String> list) {
        int amount = this.getAmount();

        amount *= getPbvAmountFactor(serverChecker, pcxnPriceText);
        amount *= getTransactionAmountFactor(list);

        return amount;
    }

    @Override
    public @NotNull Map<String, DataAccess> getSearchData() {
        return searchData;
    }

    @Override
    public @Nullable JsonObject findItemInfo(String dataKey) {
        if (PriceCxn.getMod().getCxnListener().getData(dataKey) == null)
            return null;

        JsonObject obj = PriceCxn.getMod().getCxnListener().getData(dataKey).getDataObject();
        IThemeServerChecker themeChecker = PriceCxn.getMod().getCxnListener().getThemeChecker();

        if (obj == null) return null;

        if (!obj.has("mode") || !obj.has("is_nook") || !obj.has("data") || !obj.has("is_mod")) return null;
        if (!obj.get("is_mod").getAsBoolean()) return null;
        if (!obj.get("mode").getAsString().equals(themeChecker.getMode().getTranslationKey())) return null;

        JsonArray array = obj.get("data").getAsJsonArray();
        if (array.isEmpty()) return null;

        List<Integer> foundItems = new ArrayList<>();

        //item ist special_item?
        if (data.has(PriceCxnItemStackImpl.COMMENT_KEY)
                && data.get(PriceCxnItemStackImpl.COMMENT_KEY).isJsonObject()
                && data.get(PriceCxnItemStackImpl.COMMENT_KEY).getAsJsonObject().has("PublicBukkitValues")) {
            JsonObject nbtData = data.get(PriceCxnItemStackImpl.COMMENT_KEY).getAsJsonObject();
            String pbvString = nbtData.get("PublicBukkitValues").getAsJsonObject().toString();

            outer:
            for (int i = 0; i < array.size(); i++) {
                JsonObject item = array.get(i).getAsJsonObject();
                if (!item.has("item_search_key") || !item.get("item_search_key").getAsString().contains("special_item"))
                    continue;

                String searchKey = item.get("item_search_key").getAsString();
                String[] searches = searchKey.split("\\.");

                for (String s : searches) {
                    if (Objects.equals(s, "special_item")) continue;
                    if (!pbvString.contains(s)) continue outer;
                }

                foundItems.add(i);
                if (foundItems.size() > 1) return null;

            }

        }

        if (foundItems.isEmpty()) {
            outer:
            for (int i = 0; i < array.size(); i++) {
                JsonObject item = array.get(i).getAsJsonObject();
                if (!item.has("item_search_key") || item.get("item_search_key").getAsString().contains("special_item"))
                    continue;

                String searchKey = item.get("item_search_key").getAsString();
                String[] searches = searchKey.split("&c>");
                String itemNameSearch = searches[0];

                if (!getItemName().equals(itemNameSearch)) continue;

                if (searches.length > 1) {
                    String[] nbtSearches = searches[1].split("\\.");
                    if (data.get(PriceCxnItemStackImpl.COMMENT_KEY).isJsonObject()){
                        String commentSearch = data.get(PriceCxnItemStackImpl.COMMENT_KEY).getAsJsonObject().toString();

                        for (String s : nbtSearches) {
                            if (!commentSearch.contains(s)) continue outer;
                        }
                    }else {
                        PriceCxn.getMod().getLogger().warn("comment is not a JsonObject: {}", data);
                    }
                }

                foundItems.add(i);
                if (foundItems.size() > 1) return null;

            }
        }

        if (foundItems.size() == 1) {
            return array.get(foundItems.getFirst()).getAsJsonObject();
        }

        return null;
    }

    @Unique
    public int getPbvAmountFactor(@NotNull IServerChecker serverChecker, @Nullable AtomicReference<PriceText<?>> pcxnPriceText) {
        if (pcxnPrice == null
                || !pcxnPrice.has("pbv_search_key")
                || pcxnPrice.get("pbv_search_key") == JsonNull.INSTANCE
                || !this.getDataWithoutDisplay().has(PriceCxnItemStackImpl.COMMENT_KEY))
            return 1;

        String pbvKey = pcxnPrice.get("pbv_search_key").getAsString();
        JsonObject nbtData = this.getDataWithoutDisplay().get(PriceCxnItemStackImpl.COMMENT_KEY).getAsJsonObject();

        if (!nbtData.has("PublicBukkitValues")) return 1;
        JsonObject pbvData = nbtData.get("PublicBukkitValues").getAsJsonObject();
        if (!pbvData.has(pbvKey)) return 1;

        String pbvSearchResult = StringUtil.removeChars(pbvData.get(pbvKey).getAsString());

        int pbvAmount;

        try {
            pbvAmount = Integer.parseInt(pbvSearchResult);
        } catch (NumberFormatException e) {
            PriceCxn.getMod().getLogger().error("fehler beim konvertieren des pbv Daten im Item: ", e);
            return 1;
        }

        if (StorageItemStack.isOf(pcxnPrice)) {

            storageItemStack.setup(pcxnPrice, serverChecker.getWebsocket());
            if (pcxnPriceText != null)
                pcxnPriceText.set(storageItemStack.getText());
            storageItemStack.search(pbvAmount).block();

        } else {

            return pbvAmount;

        }

        return 1;
    }

    public int getTransactionAmountFactor(@Nullable List<String> list) {
        IMinecraftClient client = PriceCxn.getMod().getMinecraftClient();
        if (client.isCurrentScreenNull())
            return 1;

        String inventoryTitle = client.getInventory().getTitle();
        ;
        if (inventoryTitle == null)
            return 1;

        if (!TranslationDataAccess.TRANSACTION_TITLE.getData().getData().contains(inventoryTitle))
            return 1;

        if (list != null) {
            for (String text : list) {
                for (String datum : TranslationDataAccess.TRANSACTION_COUNT.getData().getData()) {
                    if (text.contains(datum)) {
                        String amount = StringUtil.removeChars(text);
                        try {
                            return Integer.parseInt(amount);
                        } catch (NumberFormatException e) {
                            PriceCxn.getMod().getLogger().error("fehler beim konvertieren des transaction amount: ", e);
                        }
                        break;
                    }
                }
            }
        }

        return 1;
    }

}
