package de.alive.pricecxn.mixin;

import de.alive.pricecxn.PriceCxnModClient;
import de.alive.pricecxn.cytooxien.CxnListener;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow
    private Text displayName;

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void getDisplayName(CallbackInfoReturnable<Text> ci) {
        Text originalDisplayName = this.displayName;
        if(originalDisplayName == null) return;

        CxnListener listener = PriceCxnModClient.CXN_LISTENER;

        if(!listener.isOnServer().get()) return;
        if(!listener.isActive().get()) return;

        Optional<List<String>> optional = listener.getModUsers();

        if(optional.isEmpty()) return;

        String[] strings = originalDisplayName.getString().split(" ");
        List<String> displayList = new ArrayList<>(Arrays.asList(strings));

        if(displayList.size() != 2) return;

        String playerName = displayList.get(1).replace(" ", "");

        if(!optional.get().contains(playerName)) return;

        MutableText text = MutableText.of(new PlainTextContent.Literal("")).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
        text.append(originalDisplayName).append("\uE202 ");

        ci.setReturnValue(text);
    }

}
