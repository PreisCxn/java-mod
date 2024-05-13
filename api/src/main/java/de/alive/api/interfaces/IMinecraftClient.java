package de.alive.api.interfaces;

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
