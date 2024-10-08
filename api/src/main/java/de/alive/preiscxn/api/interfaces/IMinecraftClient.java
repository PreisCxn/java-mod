package de.alive.preiscxn.api.interfaces;

public interface IMinecraftClient {
    boolean isPlayerNull();
    boolean isCurrentScreenNull();
    boolean isCurrentScreenHandlerNull();

    String getPlayerUuidAsString();

    boolean isCurrentScreenInstanceOfHandledScreen();

    IScreenHandler getScreenHandler();

    IInventory getInventory();

    boolean isCurrentServerEntryNull();

    String getCurrentServerAddress();

    void sendTranslatableMessage(String translatable, boolean overlay, boolean italic, String... args);
    void sendStyledTranslatableMessage(String translatable, boolean overlay, Object style, String... args);

    String getLanguage();

    void sendMessage(Object message);

    void openUrl(String url);
}
