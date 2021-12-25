package org.snipsniper.utils;

import org.snipsniper.utils.enums.LogLevel;

import java.time.LocalDateTime;

public class LogMessage {
    private final LogLevel level;
    private final String message;
    private final boolean printStackTrace;
    private final LocalDateTime time;

    public LogMessage(LogLevel level, String message, boolean printStackTrace, LocalDateTime time) {
        this.level = level;
        this.message = message;
        this.printStackTrace = printStackTrace;
        this.time = time;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public boolean getPrintStackTrace() {
        return printStackTrace;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
