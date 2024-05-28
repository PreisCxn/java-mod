package de.alive.preiscxn.v1_20_6.mixins;

import com.mojang.authlib.GameProfile;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.ICxnListener;
import de.alive.preiscxn.impl.cytooxien.util.DisplayNameUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public abstract class PlayerListEntryMixin {

    @Shadow @Final private GameProfile profile;

    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void getDisplayName(@NotNull CallbackInfoReturnable<Component> ci) {
        if (!PriceCxn.getMod().getConfig().isDisplayCoin()) {
            return;
        }

        Component originalDisplayName = ci.getReturnValue();
        if (originalDisplayName == null) return;

        ICxnListener listener = PriceCxn.getMod().getCxnListener();

        if (!listener.isOnServer().get()) return;
        if (!listener.isActive()) return;

        if(DisplayNameUtil.shouldDisplayCoinInTabList(listener, originalDisplayName.getString(), this.profile.getId())){
            return;
        }

        MutableComponent text = Component.literal("").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));

        ci.setReturnValue(text.append(originalDisplayName).append("\uE202 "));
    }

}
