package de.alive.preiscxn.core.events;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.interfaces.IKeyBinding;
import de.alive.preiscxn.core.PriceCxnConfiguration;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class KeyListener {
    private final PriceCxnConfiguration configuration;
    private final List<IKeyBinding> keyBindings;
    public KeyListener(PriceCxnConfiguration configuration){
        this.configuration = configuration;
        this.keyBindings = new ArrayList<>();
    }
    @Subscribe
    public void onKeyEvent(KeyEvent event) {
        for (IKeyBinding keyBinding : keyBindings) {
            /*if (keyBinding.matchesKey(event.key().) == event.key().getId()) {
                keyBinding.getKeybindExecutor().onKeybindPressed(PriceCxn.getMod().getMinecraftClient(), PriceCxn.getMod().createInventory().getMainHandStack());
            }*/
        }
    }

    public void registerKeyBinding(IKeyBinding keyBinding) {
        keyBindings.add(keyBinding);
    }
}
