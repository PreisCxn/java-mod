package de.alive.pricecxn.keybinds;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public interface KeybindExecutor {
    Map<Class<? extends KeybindExecutor>, KeyBinding> CLASS_KEY_BINDING_MAP = new HashMap<>();
    Map<KeyBinding, KeybindExecutor> KEY_BINDING_KEYBIND_EXECUTOR_MAP = new HashMap<>();

    static void register(@NotNull KeyBinding keyBinding, @NotNull KeybindExecutor keybindExecutor, boolean inInventory) {
        CLASS_KEY_BINDING_MAP.put(keybindExecutor.getClass(), keyBinding);
        if (inInventory)
            KEY_BINDING_KEYBIND_EXECUTOR_MAP.put(keyBinding, keybindExecutor);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.wasPressed() && client.player != null) {
                keybindExecutor.onKeybindPressed(
                        client.player.getInventory().getMainHandStack()
                );
            }
        });
    }

    void onKeybindPressed(ItemStack itemStack);

}
