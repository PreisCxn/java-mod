package de.alive.preiscxn.fabric.v1_20_4.impl;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.api.interfaces.IInventory;
import de.alive.preiscxn.api.interfaces.IMinecraftClient;
import de.alive.preiscxn.api.interfaces.IScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public final class MinecraftClientImpl implements IMinecraftClient {
    private final MinecraftClient minecraftClient;

    public MinecraftClientImpl(MinecraftClient minecraftClient) {
        this.minecraftClient = minecraftClient;
    }
    @Override
    public boolean isPlayerNull() {
        return minecraftClient.player == null;
    }

    @Override
    public boolean isCurrentScreenNull() {
        return minecraftClient.currentScreen == null;
    }

    @Override
    public boolean isCurrentScreenHandlerNull() {
        return minecraftClient.player == null || minecraftClient.player.currentScreenHandler == null;
    }

    @Override
    public String getPlayerUuidAsString() {
        return minecraftClient.player == null ? "" : minecraftClient.player.getUuidAsString();
    }

    @Override
    public String getPlayerNameString() {
        return minecraftClient.player == null ? "" : minecraftClient.player.getName().getString();
    }

    @Override
    public boolean isCurrentScreenInstanceOfHandledScreen() {
        return minecraftClient.currentScreen instanceof HandledScreen;
    }

    @Override
    public IScreenHandler getScreenHandler() {
        return minecraftClient.player != null ? ScreenHandlerImpl.getInstance(minecraftClient.player.currentScreenHandler) : null;
    }

    public IInventory getInventory() {
        return minecraftClient.player != null ? new InventoryImpl(minecraftClient) : null;
    }

    @Override
    public boolean isCurrentServerEntryNull() {
        return minecraftClient.getCurrentServerEntry() == null;
    }

    @Override
    public String getCurrentServerAddress() {
        return minecraftClient.getCurrentServerEntry() == null ? "" : minecraftClient.getCurrentServerEntry().address;
    }

    @Override
    public void sendTranslatableMessage(String translatable, boolean overlay, boolean italic, String... args) {
        if (minecraftClient.player == null)
            return;
        Text text = Text.translatable(translatable, (Object[]) args);
        if(italic)
            text = text.copy().styled(style -> style.withItalic(true));

        minecraftClient.player.sendMessage(
                text,
                overlay
        );
    }

    @Override
    public void sendStyledTranslatableMessage(String translatable, boolean overlay, Object style, String... args) {
        if (minecraftClient.player == null)
            return;

        minecraftClient.player.sendMessage(
                ((Text)PriceCxn.getMod().getModText()).copy()
                        .append(Text.translatable(translatable, (Object[]) args))
                        .setStyle(((Style) style)),
                overlay
        );

    }

    @Override
    public String getLanguage() {
        return minecraftClient.getLanguageManager().getLanguage();
    }

    @Override
    public void sendMessage(Object message) {
        if (minecraftClient.player == null)
            return;

        minecraftClient.player.sendMessage((Text) message, false);
    }

    @Override
    public void openUrl(String url) {
        Util.getOperatingSystem().open(url);
    }
}
