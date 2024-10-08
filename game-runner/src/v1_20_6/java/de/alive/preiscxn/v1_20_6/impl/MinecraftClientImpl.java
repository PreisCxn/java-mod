package de.alive.preiscxn.v1_20_6.impl;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.interfaces.IInventory;
import de.alive.preiscxn.api.interfaces.IScreenHandler;
import de.alive.preiscxn.api.utils.UUIDHasher;
import de.alive.preiscxn.core.impl.LabyMinecraftClient;
import net.labymod.api.models.Implements;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.net.MalformedURLException;
import java.net.URI;

@Implements(LabyMinecraftClient.class)
public final class MinecraftClientImpl implements LabyMinecraftClient {
    private Minecraft minecraftClient;

    public MinecraftClientImpl() {

    }

    MinecraftClientImpl setMinecraftClient(Minecraft minecraftClient) {
        this.minecraftClient = minecraftClient;
        return this;
    }

    @Override
    public boolean isPlayerNull() {
        return minecraftClient.player == null;
    }

    @Override
    public boolean isCurrentScreenNull() {
        return minecraftClient.screen == null;
    }

    @Override
    public boolean isCurrentScreenHandlerNull() {
        return minecraftClient.player == null;
    }

    @Override
    public String getPlayerUuidAsString() {
        return minecraftClient.player == null ? "" : UUIDHasher.hash(minecraftClient.player.getUUID()).toString();
    }

    @Override
    public boolean isCurrentScreenInstanceOfHandledScreen() {
        return minecraftClient.screen instanceof InventoryScreen;
    }

    @Override
    public IScreenHandler getScreenHandler() {
        return minecraftClient.player != null ? new ScreenHandlerImpl().setScreenHandler(minecraftClient.player.containerMenu) : null;
    }

    public IInventory getInventory() {
        return minecraftClient.player != null ? new InventoryImpl().setMinecraftClient(minecraftClient) : null;
    }

    @Override
    public boolean isCurrentServerEntryNull() {
        return minecraftClient.getCurrentServer() == null;
    }

    @Override
    public String getCurrentServerAddress() {
        return minecraftClient.getCurrentServer() == null ? "" : minecraftClient.getCurrentServer().ip;
    }

    @Override
    public void sendTranslatableMessage(String translatable, boolean overlay, boolean italic, String... args) {
        if (minecraftClient.player == null)
            return;

        Component[] components = new Component[args.length];
        for (int i = 0; i < args.length; i++) {
            components[i] = Component.literal(args[i]);
        }
        Component text = Component.translatable(translatable, (Object[]) components);
        if(italic)
            text = text.copy().withStyle(style -> style.withItalic(true));

        minecraftClient.player.displayClientMessage(
                text,
                overlay
        );
    }

    @Override
    public void sendStyledTranslatableMessage(String translatable, boolean overlay, Object style, String... args) {
        if (minecraftClient.player == null)
            return;

        Component[] components = new Component[args.length];
        for (int i = 0; i < args.length; i++) {
            components[i] = Component.literal(args[i]);
        }
        Style style2 = (Style) style;
        Component
                .translatable(translatable, (Object[]) components)
                .withStyle((style2::applyTo));

        minecraftClient.player.displayClientMessage(
                Component.translatable(translatable, (Object[]) components).withStyle((style2::applyTo)),
                overlay
        );
    }

    @Override
    public String getLanguage() {
        return minecraftClient.getLanguageManager().getSelected();
    }

    @Override
    public void sendMessage(Object message) {
        if (minecraftClient.player == null)
            return;

        switch (message) {
            case Component component:
                minecraftClient.player.displayClientMessage(component, false);
            case null:
                break;
            default:
                minecraftClient.player.displayClientMessage(Component.literal(message.toString()), false);
        }
    }

    @Override
    public void openUrl(String url) {
        try {
            Util.getPlatform().openUrl(URI.create(url).toURL());
        } catch (MalformedURLException e) {
            PriceCxn.getMod().getLogger().error("Failed to open URL: " + url, e);
        }
    }
}
