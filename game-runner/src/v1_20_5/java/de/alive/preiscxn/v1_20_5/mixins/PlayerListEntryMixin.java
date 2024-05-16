package de.alive.preiscxn.v1_20_5.mixins;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.cytooxien.ICxnListener;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(PlayerInfo.class)
public abstract class PlayerListEntryMixin {
    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void getDisplayName(@NotNull CallbackInfoReturnable<Component> ci) {
        Component originalDisplayName = ci.getReturnValue();
        if (originalDisplayName == null) return;

        ICxnListener listener = PriceCxn.getMod().getCxnListener();

        if (!listener.isOnServer().get()) return;
        if (!listener.isActive()) return;

        List<String> modUsers = listener.getModUsers();

        if (modUsers == null) return;

        String[] strings = originalDisplayName.getString().split(" ");
        List<String> displayList = new ArrayList<>(Arrays.asList(strings));

        if (displayList.size() != 2) return;

        String playerName = displayList.get(1).replace(" ", "");

        if (!modUsers.contains(playerName)) return;

        MutableComponent text = Component.literal("").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));

        ci.setReturnValue(text.append(originalDisplayName).append("\uE202 "));
    }

}
