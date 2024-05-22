package de.alive.preiscxn.core;

import de.alive.preiscxn.api.interfaces.PriceCxnConfig;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.key.Key;
import net.labymod.api.client.gui.screen.widget.widgets.input.KeybindWidget.KeyBindSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@ConfigName("settings")
public class PriceCxnConfiguration extends AddonConfig implements PriceCxnConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> displayCoin = new ConfigProperty<>(true);

    @SwitchSetting
    @KeyBindSetting
    private final ConfigProperty<Key> openInBrowser = new ConfigProperty<>(Key.H);

    @SwitchSetting
    @KeyBindSetting
    private final ConfigProperty<Key> cycleAmount = new ConfigProperty<>(Key.R_BRACKET);

    @Override
    public ConfigProperty<Boolean> enabled() {
        return this.enabled;
    }

    public ConfigProperty<Key> getOpenInBrowser() {
        return openInBrowser;
    }

    public ConfigProperty<Key> getCycleAmount() {
        return cycleAmount;
    }

    @Override
    public boolean isActive() {
        return enabled.get();
    }

    @Override
    public boolean isDisplayCoin() {
        return displayCoin.get() && enabled.get();
    }
}
