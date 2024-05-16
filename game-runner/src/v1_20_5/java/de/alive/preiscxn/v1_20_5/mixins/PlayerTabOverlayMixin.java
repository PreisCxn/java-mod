package de.alive.preiscxn.v1_20_5.mixins;

import de.alive.preiscxn.api.interfaces.VersionedTabGui;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin implements VersionedTabGui {
    @Nullable
    @Shadow
    private Component footer;
    @Nullable
    @Shadow
    private Component header;

    @Unique
    @Override
    public String priceCxn$getHeader() {
        return header == null ? "" : header.getString();
    }

    @Override
    public String priceCxn$getFooter() {
        return footer == null ? "" : footer.getString();
    }
}
