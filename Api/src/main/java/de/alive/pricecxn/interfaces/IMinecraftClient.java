package de.alive.pricecxn.interfaces;

public interface IMinecraftClient {
    boolean isPlayerNull();
    boolean isCurrentScreenNull();
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
