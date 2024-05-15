package de.alive.preiscxn.api.cytooxien;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum ActionNotification {
    SERVER_OFFLINE("cxn_listener.server_connection.error"),
    SERVER_MAINTENANCE("cxn_listener.server_connection.maintenance"),
    SERVER_MAINTEANCE_WITH_PERMISSON("cxn_listener.server_connection.maintenance_with_permission"),
    WRONG_VERSION("cxn_listener.wrong_version"),
    MOD_STARTED("cxn_listener.mod_started"),
    SERVER_ONLINE("cxn_listener.server_connection.success"),
    MOD_STOPPED("cxn_listener.mod_stopped");

    private final String translationKey;
    private String[] variables;

    ActionNotification(String translationKey) {
        this.translationKey = translationKey;
        this.variables = new String[0];
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public void setTextVariables(String... variables) {
        this.variables = variables;
    }

    public String @NotNull [] getTextVariables() {
        String[] varBackup = Arrays.copyOf(this.variables, this.variables.length);
        this.variables = new String[0];
        return varBackup;
    }

    public boolean hasTextVariables() {
        return this.variables.length > 0;
    }

}
