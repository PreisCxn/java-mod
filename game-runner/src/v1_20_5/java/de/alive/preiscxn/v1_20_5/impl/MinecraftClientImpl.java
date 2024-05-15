package de.alive.preiscxn.v1_20_5.impl;

import de.alive.preiscxn.api.interfaces.IInventory;
import de.alive.preiscxn.api.interfaces.IScreenHandler;
import de.alive.preiscxn.core.impl.LabyMinecraftClient;
import net.labymod.api.models.Implements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

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
        return minecraftClient.player == null ? "" : minecraftClient.player.getStringUUID();
    }

    @Override
    public String getPlayerNameString() {
        return minecraftClient.player == null ? "" : minecraftClient.player.getName().getString();
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
}
