package de.alive.preiscxn.api.cytooxien;

import de.alive.preiscxn.api.utils.UUIDHasher;

import java.util.UUID;

public class ModUser {

    private final String name;
    private final UUID uuid;

    public ModUser(String name) {
        this.name = name;
        this.uuid = null;
    }

    public ModUser(UUID uuid) {
        this.uuid = uuid;
        this.name = null;
    }

    public boolean isCorrect(String name, UUID uuid) {
        uuid = UUIDHasher.hash(uuid);
        return (this.name != null && this.name.equals(name)) || (this.uuid != null && this.uuid.equals(uuid));
    }
}
