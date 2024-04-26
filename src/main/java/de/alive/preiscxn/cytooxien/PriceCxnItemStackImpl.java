package de.alive.preiscxn.cytooxien;

import com.google.gson.*;
import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.IThemeServerChecker;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.networking.DataAccess;
import de.alive.api.utils.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static de.alive.api.LogPrinter.LOGGER;

public class PriceCxnItemStackImpl implements PriceCxnItemStack {
    private static final Pattern JSON_KEY_PATTERN = Pattern.compile("([{,])(\\w+):");
    private static final Pattern TO_DELETE_PATTERN = Pattern.compile("[\\\\']");

    private final @NotNull ItemStack item;

    private final @NotNull Map<String, DataAccess> searchData;

    private final JsonObject data = new JsonObject();

    private String itemName;

    private final String displayName;

    private int amount = 0;

    private List<String> toolTips;

    public PriceCxnItemStackImpl(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {

        this.searchData = Objects.requireNonNullElseGet(searchData, HashMap::new);

        this.item = item;
        if(addTooltips)
            this.toolTips = StringUtil.getToolTips(this.item);
        this.itemName = this.item.getItem().getTranslationKey();
        this.displayName = this.item.getName().getString();
        this.amount = item.getCount();

        if(item.isIn(ItemTags.TRIM_TEMPLATES) || item.isOf(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) {

            Optional<RegistryKey<Item>> key = item.getRegistryEntry().getKey();
            key.map(itemRegistryKey -> this.itemName += "." + itemRegistryKey.getValue().getPath());

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
        data.addProperty(MC_CLIENT_LANG_KEY, MinecraftClient.getInstance().getLanguageManager().getLanguage());
        if (addComment)
            data.add(COMMENT_KEY, getCustomData(this.item));

        LOGGER.debug(itemName);


        /*
        zusätzlich suche nach den keys in searchData:
         */

        for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {

            DataAccess access = entry.getValue();
            JsonElement result = access.getDefaultResult();
            String searchResult = this.toolTipSearch(access);

            if (searchResult != null) {
                if (entry.getValue().hasProcessData()) {
                    result = access.getProcessData().apply(new JsonPrimitive(searchResult));
                } else {
                    result = new JsonPrimitive(searchResult);
                }
            }

            data.add(entry.getKey(), result);
        }

    }

    public PriceCxnItemStackImpl(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment) {
        this(item, searchData, addComment, true);
    }

    public PriceCxnItemStackImpl(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData) {
        this(item, searchData, true);
    }

    private @NotNull JsonObject getCustomData(@NotNull ItemStack item) {
        ComponentMap nbt = item.getComponents();
        if (nbt == null) return new JsonObject();

        return componentMapToJson(nbt).getAsJsonObject("minecraft:custom_data");
    }

    private @NotNull JsonObject componentMapToJson(@NotNull ComponentMap componentMap) {
        JsonObject json = new JsonObject();

        for (DataComponentType<?> key : componentMap.getTypes()) {
            Object component = componentMap.get(key);
            if (component == null)
                continue;

            if(component instanceof ComponentMap subComponentMap){
                json.add(key.toString(), componentMapToJson(subComponentMap));
            } else {
                Object object = object(component.toString());
                if (object instanceof JsonElement element)
                    json.add(key.toString(), element);
                else
                    json.addProperty(key.toString(), object.toString());
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
            }

            //test if JsonKey is missing
            try {
                return JsonParser.parseString(nbtString).getAsJsonObject();
            } catch (IllegalStateException e2) {
                //else add as normal String
                return nbtString;
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

    private @Nullable String toolTipSearch(@NotNull DataAccess access) {
        String result;
        for (String prefix : access.getData()) {
            result = StringUtil.getFirstSuffixStartingWith(this.toolTips, prefix);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public int hashCode() {
        JsonObject hash = this.data.deepCopy();

        for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {
            if (entry.getValue().hasEqualData()) {
                hash.remove(entry.getKey());
            }
        }

        return hash.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o == this) || (o instanceof PriceCxnItemStackImpl
                && ((PriceCxnItemStackImpl) o).getEqualData().equals(this.getEqualData()));
    }

    @Override
    public boolean deepEquals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PriceCxnItemStackImpl item)) return false;

        for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {
            if (entry.getValue().hasEqualData()) {

                JsonElement el1 = item.getData().get(entry.getKey());
                JsonElement el2 = this.getData().get(entry.getKey());

                if (!entry.getValue().getEqualData().apply(new Pair<>(el1, el2)))
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

        if (hash.has(COMMENT_KEY) && hash.getAsJsonObject(COMMENT_KEY).has("display"))
            hash.get(COMMENT_KEY).getAsJsonObject().remove("display");

        for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {
            if (entry.getValue().hasEqualData()) {
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
            if (entry.getValue().hasEqualData()) {
                if (!item.getSearchData().containsKey(entry.getKey())) continue;
                if (!item.getSearchData().get(entry.getKey()).hasEqualData()) continue;

                this.data.remove(entry.getKey());
                this.data.add(entry.getKey(), item.getData().get(entry.getKey()));
            }
        }
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public @NotNull Map<String, DataAccess> getSearchData() {
        return searchData;
    }

    @Override
    public @Nullable JsonObject findItemInfo(String dataKey) {
        if(PriceCxn.getMod().getCxnListener().getData(dataKey) == null)
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
        if (data.has(PriceCxnItemStackImpl.COMMENT_KEY) &&
            data.get(PriceCxnItemStackImpl.COMMENT_KEY).isJsonObject() &&
            data.get(PriceCxnItemStackImpl.COMMENT_KEY).getAsJsonObject().has("PublicBukkitValues")) {
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
                    if(s.equals("special_item")) continue;
                    if (!pbvString.contains(s)) continue outer;
                }

                foundItems.add(i);
                if (foundItems.size() > 1) return null;

            }

        }

        if(foundItems.isEmpty()) {
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
                    String commentSearch = data.get(PriceCxnItemStackImpl.COMMENT_KEY).getAsJsonObject().toString();

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
