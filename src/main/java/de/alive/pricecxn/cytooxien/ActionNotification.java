package de.alive.pricecxn.cytooxien;

public enum ActionNotification {
    SERVER_OFFLINE("Der Server ist offline"),
    SERVER_MAINTENANCE("Der Server ist im Wartungsmodus"),
    SERVER_MAINTEANCE_WITH_PERMISSON("Der Server ist im Wartungsmodus, du hast aber die Berechtigung, ihn zu betreten"),
    WRONG_VERSION("Die Version des Servers ist nicht kompatibel mit der des Mods"),
    MOD_STARTED("Der Mod wurde gestartet");

    private final String message;

    ActionNotification(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
