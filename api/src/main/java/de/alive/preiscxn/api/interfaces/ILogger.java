package de.alive.preiscxn.api.interfaces;

public interface ILogger {
    void info(String message, Object... args);
    void warn(String message, Object... args);
    void error(String message, Object... args);
    void error(String message, Throwable throwable, Object... args);
    void debug(String message, Object... args);
}
