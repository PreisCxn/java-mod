package de.alive.preiscxn.core.impl;

import de.alive.preiscxn.api.interfaces.ILogger;
import net.labymod.api.util.logging.Logging;

public class LoggerImpl implements ILogger {
    private final Logging logging;

    public LoggerImpl(Logging logging) {
        this.logging = logging;
    }

    @Override
    public void info(String message, Object... args) {
        this.logging.info(message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        this.logging.warn(message, args);
    }

    @Override
    public void error(String message, Object... args) {
        this.logging.error(message, args);
    }

    @Override
    public void error(String message, Throwable throwable, Object... args) {
        this.logging.error(message, args);
        this.logging.error(message, throwable);
    }

    @Override
    public void debug(String message, Object... args) {
        this.logging.debug(message, args);
    }
}
