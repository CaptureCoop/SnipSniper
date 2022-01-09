package net.snipsniper.utils;

import net.snipsniper.utils.enums.LogLevel;

import java.time.LocalDateTime;

public class LogMessage {
    private final LogLevel level;
    private final String message;
    private final LocalDateTime time;

    public LogMessage(LogLevel level, String message, LocalDateTime time) {
        this.level = level;
        this.message = message;
        this.time = time;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
