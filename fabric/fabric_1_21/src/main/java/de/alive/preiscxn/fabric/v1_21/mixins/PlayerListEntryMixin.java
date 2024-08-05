package de.alive.preiscxn.fabric.v1_21.mixins;

import com.mojang.authlib.GameProfile;
import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.ICxnListener;
import de.alive.preiscxn.impl.cytooxien.util.DisplayNameUtil;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow
    private Text displayName;

    @Shadow @Final private GameProfile profile;

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void getDisplayName(@NotNull CallbackInfoReturnable<Text> ci) {
        if (!PriceCxn.getMod().getConfig().isDisplayCoin()) {
            return;
        }

        Text originalDisplayName = this.displayName;
        if (originalDisplayName == null) return;

        ICxnListener listener = PriceCxn.getMod().getCxnListener();

        if (!listener.isOnServer().get()) return;
        if (!listener.isActive()) return;

        if(!DisplayNameUtil.shouldDisplayCoinInTabList(listener, originalDisplayName.getString(), this.profile.getId())){
            return;
        }
        MutableText text = MutableText.of(new PlainTextContent.Literal("")).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
        text.append(originalDisplayName).append("\uE202 ");

        ci.setReturnValue(text);
    }

}
