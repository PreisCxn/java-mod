package de.alive.pricecxn.mixin;

import de.alive.pricecxn.keybinds.KeybindExecutor;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Inject(
            method = "keyPressed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;handleHotbarKeyPressed(II)Z")
    )
    public void on(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
            KeybindExecutor.KEY_BINDING_KEYBIND_EXECUTOR_MAP.forEach(
                    (keyBinding, keybindExecutor) -> {
                        if (keyBinding.matchesKey(keyCode, scanCode)) {
                            keybindExecutor.onKeybindPressed(this.focusedSlot.getStack());
                        }
                    }
            );
        }
    }

}