package de.alive.pricecxn.mixin;

import de.alive.pricecxn.PriceCxnMod;
import de.alive.pricecxn.PriceCxnModClient;
import de.alive.pricecxn.networking.ServerChecker;
import de.alive.pricecxn.cytooxien.Modes;
import de.alive.pricecxn.cytooxien.ThemeServerChecker;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract void removeCustomName();

    @Shadow
    public abstract Item getItem();

    @Shadow
    private int count;

    @Inject(method = "getTooltip", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void getToolTip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> callbackInfoReturnable, List<Text> list) {

        ServerChecker serverChecker = PriceCxnModClient.CXN_LISTENER.getServerChecker();
        ThemeServerChecker themeChecker = PriceCxnModClient.CXN_LISTENER.getThemeChecker();

        if (serverChecker == null) return;
        if (themeChecker == null) return;

        Modes mode = themeChecker.getMode();

        if (mode == Modes.NOTHING || mode == Modes.LOBBY) return;


        list.add(
                MutableText.of(new LiteralTextContent("--- "))
                        .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))
                        .append(PriceCxnMod.MOD_TEXT.copy())
                        .append(MutableText.of(new LiteralTextContent("---"))
                                .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))));
        list.add(Text.of(themeChecker.getMode().toString()));


    }


}
