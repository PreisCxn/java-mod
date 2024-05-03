package de.alive.preiscxn.mixin;

import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.ICxnListener;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow
    private Text displayName;

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void getDisplayName(@NotNull CallbackInfoReturnable<Text> ci) {
        Text originalDisplayName = this.displayName;
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

        MutableText text = MutableText.of(new PlainTextContent.Literal("")).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
        text.append(originalDisplayName).append("\uE202 ");

        ci.setReturnValue(text);
    }

}
