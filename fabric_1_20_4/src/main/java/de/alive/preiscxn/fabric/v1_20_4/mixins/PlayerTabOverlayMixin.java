package de.alive.preiscxn.fabric.v1_20_4.mixins;

import de.alive.preiscxn.api.interfaces.VersionedTabGui;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerListHud.class)
public class PlayerTabOverlayMixin implements VersionedTabGui {

    @Shadow @Nullable private Text header;

    @Shadow @Nullable private Text footer;

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
