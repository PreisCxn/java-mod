package de.alive.preiscxn.fabric.impl;

import de.alive.preiscxn.api.interfaces.ILogger;
import org.slf4j.Logger;

public class LoggerImpl implements ILogger {
    private final Logger logger;

    public LoggerImpl(Logger logger) {
        this.logger = logger;
    }


    @Override
    public void info(String message, Object... args) {
        this.logger.info(message,
                args);
    }

    @Override
    public void warn(String message, Object... args) {
        this.logger.warn(message,
                args);
    }

    @Override
    public void error(String message, Object... args) {
        this.logger.error(message,
                args);
    }

    @Override
    public void error(String message, Throwable throwable, Object... args) {
        this.logger.error(message,
                throwable,
                args);
    }

    @Override
    public void debug(String message, Object... args) {
        this.logger.debug(message,
                args);
    }
}
