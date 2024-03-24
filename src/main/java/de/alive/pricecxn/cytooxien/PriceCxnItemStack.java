package de.alive.pricecxn.cytooxien;

import com.google.gson.*;
import de.alive.pricecxn.networking.DataAccess;
import de.alive.pricecxn.utils.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class PriceCxnItemStack {

    private static final Pattern JSON_KEY_PATTERN = Pattern.compile("([{,])(\\w+):");
    private static final Pattern TO_DELETE_PATTERN = Pattern.compile("[\\\\']");

    public static final String ITEM_NAME_KEY = "itemName";
    public static final String AMOUNT_KEY = "amount";
    public static final String COMMENT_KEY = "comment";
    public static final String DISPLAY_NAME_KEY = "displayName";
    public static final String MC_CLIENT_LANG_KEY = "mcClientLang";

    private final ItemStack item;

    private final Map<String, DataAccess> searchData;

    private final JsonObject data = new JsonObject();

    private String itemName;

    private String displayName;

    private int amount = 0;

    private List<String> toolTips;

    public PriceCxnItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips) {

        if (searchData == null)
            this.searchData = new HashMap<>();
        else
            this.searchData = searchData;

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
            data.add(COMMENT_KEY, nbtToJson(this.item));

        System.out.println(itemName);


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

    public PriceCxnItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment) {
        this(item, searchData, addComment, true);
    }

    public PriceCxnItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData) {
        this(item, searchData, true);
    }

    private JsonObject nbtToJson(ItemStack item) {
        JsonObject json = new JsonObject();
        NbtCompound nbt = item.getNbt();

        if (!item.hasNbt()) return json;
        if (nbt == null) return json;

        for (String key : nbt.getKeys()) {
            if (nbt.contains(key)) {
                String nbtString = Optional.ofNullable(nbt.get(key)).map(NbtElement::asString).orElse(null);

                if (nbtString == null) continue;

                nbtString = TO_DELETE_PATTERN.matcher(nbtString).replaceAll("");

                JsonObject valueJson = null;

                //test if only Delete Pattern is needed
                try {
                    valueJson = JsonParser.parseString(nbtString).getAsJsonObject();
                } catch (IllegalStateException e) {
                    nbtString = JSON_KEY_PATTERN.matcher(nbtString).replaceAll("$1\"$2\":");

                    //test if JsonArray
                    try {
                        JsonArray array = JsonParser.parseString(nbtString).getAsJsonArray();
                        json.add(key, array);
                        continue;
                    } catch (IllegalStateException ignored) {
                    }

                    //test if JsonKey is missing
                    try {
                        valueJson = JsonParser.parseString(nbtString).getAsJsonObject();
                    } catch (IllegalStateException e2) {
                        //else add as normal String
                        json.addProperty(key, Optional.ofNullable(nbt.get(key)).map(NbtElement::asString).orElse("null"));
                    }

                } catch (JsonParseException e) {
                    //else add as normal String
                    json.addProperty(key, Optional.ofNullable(nbt.get(key)).map(NbtElement::asString).orElse("null"));
                }

                if (valueJson != null) {
                    json.add(key, valueJson);
                }
            }
        }

        return json;
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

        if (this.searchData == null) return hash.hashCode();

        for (Map.Entry<String, DataAccess> entry : this.searchData.entrySet()) {
            if (entry.getValue().hasEqualData()) {
                hash.remove(entry.getKey());
            }
        }

        return hash.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o == this) || (o instanceof PriceCxnItemStack
                && ((PriceCxnItemStack) o).getEqualData().equals(this.getEqualData()));
    }

    public boolean deepEquals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PriceCxnItemStack item)) return false;

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

    public JsonObject getData() {
        return data;
    }

    public JsonObject getDataWithoutDisplay() {
        JsonObject data = this.data.deepCopy();
        data.get(COMMENT_KEY).getAsJsonObject().remove("display");
        return data;
    }

    private JsonObject getEqualData() {
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

    public boolean isSameItem(PriceCxnItemStack item) {
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

    public String getItemName() {
        return itemName;
    }

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

    public int getAmount() {
        return amount;
    }

    public @NotNull Map<String, DataAccess> getSearchData() {
        return searchData;
    }

}
