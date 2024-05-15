package de.alive.preiscxn.api;

import java.util.Objects;

import static de.alive.preiscxn.api.LogPrinter.LOGGER;

public final class PriceCxn {
    public static final String RELEASE_CHANNEL;

    static {
        RELEASE_CHANNEL = Objects.equals(System.getenv("PCXN_RELEASE_CHANNEL"), "release")
                          || System.getenv("PCXN_RELEASE_CHANNEL") == null
                ? null
                : System.getenv("PCXN_RELEASE_CHANNEL").toLowerCase();
        LOGGER.info("Release channel: {}", RELEASE_CHANNEL);
    }

    private PriceCxn() {
    }

    private static Mod mod;

    public static Mod getMod() {
        return mod;
    }
}
