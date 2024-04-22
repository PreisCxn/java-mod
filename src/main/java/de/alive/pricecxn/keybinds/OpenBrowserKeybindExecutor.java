package de.alive.pricecxn.keybinds;

import com.google.gson.JsonObject;
import de.alive.pricecxn.cytooxien.PriceCxnItemStackImpl;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

public class OpenBrowserKeybindExecutor implements KeybindExecutor {
    private static final String URL_PREFIX = "https://preiscxn.de/";
    @Override
    public void onKeybindPressed(@NotNull ItemStack itemStack) {
        PriceCxnItemStackImpl priceCxnItemStackImpl = new PriceCxnItemStackImpl(itemStack, null, true, false);

        JsonObject data = priceCxnItemStackImpl.findItemInfo("pricecxn.data.item_data");

        System.out.println(data);
        if (data != null && data.has("item_info_url")) {
            String itemInfoUrl = data.get("item_info_url").getAsString();

            if (itemInfoUrl != null && !itemInfoUrl.isEmpty() && !itemInfoUrl.equals("null"))
                Util.getOperatingSystem().open(URL_PREFIX + itemInfoUrl);
        }

    }

}
