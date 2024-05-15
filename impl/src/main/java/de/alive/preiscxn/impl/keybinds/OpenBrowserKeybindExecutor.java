package de.alive.preiscxn.impl.keybinds;

import com.google.gson.JsonObject;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.PriceCxnItemStack;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.keybinds.KeybindExecutor;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OpenBrowserKeybindExecutor implements KeybindExecutor {
    private static final String URL_PREFIX = "https://preiscxn.de/";
    @Override
    public void onKeybindPressed(IMinecraftClient client, @NotNull IItemStack itemStack) {
        PriceCxnItemStack priceCxnItemStackImpl = itemStack.createItemStack(null, true, false);

        JsonObject data = priceCxnItemStackImpl.findItemInfo("pricecxn.data.item_data");

        if (data != null && data.has("item_info_url")) {
            String itemInfoUrl = data.get("item_info_url").getAsString();

            if (itemInfoUrl != null && !itemInfoUrl.isEmpty() && !Objects.equals(itemInfoUrl, "null")) {
                String amount = (itemInfoUrl.contains("?") ? "&" : "?")
                                + "amount="
                                + priceCxnItemStackImpl.getAdvancedAmount(
                                PriceCxn.getMod().getCxnListener().getServerChecker(),
                                null,
                                null
                        );

                Util.getOperatingSystem().open(URL_PREFIX + itemInfoUrl + amount);
            }
        }

    }

}
