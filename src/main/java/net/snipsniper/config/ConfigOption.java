package net.snipsniper.config;

import org.capturecoop.cclogger.CCLogger;
import org.capturecoop.cclogger.CCLogLevel;

public class ConfigOption {
    private String key;
    private String value;
    private final TYPE type;

    public ConfigOption(String key, String value) {
        this.key = key;
        this.value = value;
        type = TYPE.KEY_VALUE;
    }

    public ConfigOption(String comment) {
        if(comment.startsWith("#"))
            value = comment.substring(1);
        else
            value = comment; //Avoid reference
        type = TYPE.COMMENT;
    }

    public ConfigOption() {
        type = TYPE.NEWLINE;
    }

    public void setValue(String value) {
        this.value = value + "";
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        switch(type) {
            case KEY_VALUE: return key + "=" + value;
            case COMMENT: return "#" + value;
            case NEWLINE: return "";
        }
        CCLogger.log("NO TYPE SET! KEY: %c VALUE: %c", CCLogLevel.ERROR, key, value);
        CCLogger.logStacktrace(CCLogLevel.ERROR);
        return null;
    }

    public TYPE getType() {
        return type;
    }

    enum TYPE { KEY_VALUE, COMMENT, NEWLINE}
}
