package de.alive.preiscxn.v1_20_4.mixins;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.interfaces.IItemStack;
import de.alive.preiscxn.v1_20_4.impl.EntrypointImpl;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Inject(
            method = "keyPressed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;checkHotbarKeyPressed(II)Z")
    )
    public void on(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            PriceCxn.getMod().forEachKeybindExecutor(
                    (keyBinding, keybindExecutor) -> {
                        if (keyBinding.matchesKey(keyCode, scanCode)) {
                            keybindExecutor.onKeybindPressed(
                                    new EntrypointImpl().createMinecraftClient(),
                                    (IItemStack) (Object) this.hoveredSlot.getItem());
                        }
                    }
            );
        }
    }

}
