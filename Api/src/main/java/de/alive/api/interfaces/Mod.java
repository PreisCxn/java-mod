package de.alive.api.interfaces;

import de.alive.api.cytooxien.ICxnListener;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.networking.DataAccess;
import de.alive.api.networking.Http;
import de.alive.api.networking.cdn.CdnFileHandler;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

public interface Mod {
    PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment, boolean addTooltips);
    PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData, boolean addComment);
    PriceCxnItemStack createItemStack(@NotNull ItemStack item, @Nullable Map<String, DataAccess> searchData);

    ICxnListener getCxnListener();
    IPlayer getPlayer();
    void runOnEndClientTick(Consumer<IMinecraftClient> consumer);
    CdnFileHandler getCdnFileHandler();
    IMinecraftClient getMinecraftClient();
    Http getHttp();
}
