package de.alive.preiscxn.api.interfaces;

public interface IMinecraftClient {
    boolean isPlayerNull();
    boolean isCurrentScreenNull();
    boolean isCurrentScreenHandlerNull();

    String getPlayerUuidAsString();

    String getPlayerNameString();

    boolean isCurrentScreenInstanceOfHandledScreen();

    IScreenHandler getScreenHandler();

    IInventory getInventory();
}
