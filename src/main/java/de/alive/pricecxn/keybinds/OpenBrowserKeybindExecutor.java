package de.alive.pricecxn.keybinds;

import com.google.gson.JsonObject;
import de.alive.pricecxn.cytooxien.PriceCxnItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

public class OpenBrowserKeybindExecutor implements KeybindExecutor {

    @Override
    public void onKeybindPressed(ItemStack itemStack) {
        PriceCxnItemStack priceCxnItemStack = new PriceCxnItemStack(itemStack, null, true, false);

        JsonObject data = priceCxnItemStack.findItemInfo("pricecxn.data.item_data");

        System.out.println(data);
        if (data != null && data.has("item_info_url")) {
            String itemInfoUrl = data.get("item_info_url").getAsString();

            if (itemInfoUrl != null && !itemInfoUrl.isEmpty())
                Util.getOperatingSystem().open(itemInfoUrl);
        }

    }

}
