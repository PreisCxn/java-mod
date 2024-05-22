package de.alive.preiscxn.core.events;

import net.labymod.api.event.Subscribe;
import net.labymod.api.event.labymod.config.ConfigurationVersionUpdateEvent;

public class ConfigChangeListener {
    @Subscribe
    public void onConfigChange(ConfigurationVersionUpdateEvent event) {
        System.out.println("Config changed: " + event.getJsonObject());
    }
}
