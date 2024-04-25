package de.alive.api.interfaces;

public interface IMinecraftClient {
    boolean isPlayerNull();
    boolean isCurrentScreenNull();
    boolean isCurrentScreenHandlerNull();
    boolean isCurrentScreenTitleNull();
    String getCurrentScreenTitle();

    boolean containsInTitle(String s);

    boolean equalsTitle(String s);

    int getInventorySize();

    String getPlayerUuidAsString();

    String getPlayerNameString();

    boolean isCurrentScreenInstanceOfHandledScreen();

    IScreenHandler getScreenHandler();
}
