package org.snipsniper.config;

import java.util.ArrayList;
import java.util.HashMap;

public class ConfigContainer {
    private final ArrayList<ConfigOption> list = new ArrayList<>();
    private final HashMap<String, ConfigOption> map = new HashMap<>();

    public void set(String key, String value) {
        ConfigOption option = new ConfigOption(key, value);
        if(!map.containsKey(key)) {
            list.add(option);
            map.put(key, option);
        } else {
            for(ConfigOption currentOption : list) {
                if(currentOption.getType() == ConfigOption.TYPE.KEY_VALUE) {
                    if(currentOption.getKey().equals(key)) {
                        currentOption.setValue(value);
                        break;
                    }
                }
            }
            map.replace(key, option);
        }

    }

    public void set(String comment) {
        list.add(new ConfigOption(comment));
    }

    public void addNewLine() {
        list.add(new ConfigOption());
    }

    public String get(String key) {
        if(map.containsKey(key))
            return map.get(key).getValue();
        return null;
    }

    public void loadFromContainer(ConfigContainer container) {
        clear();
        for(ConfigOption option : container.getList()) {
            switch(option.getType()) {
                case KEY_VALUE: set(option.getKey(), option.getValue()); break;
                case COMMENT: set(option.getValue()); break;
                case NEWLINE: addNewLine(); break;
            }
        }
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        list.clear();
        map.clear();
    }

    public boolean equals(ConfigContainer other) {
        boolean isSame = true;
        for(ConfigOption option : list) {
            if(option.getKey() != null) {
                String value = option.getValue();
                String otherValue = other.get(option.getKey());
                if(otherValue != null && !otherValue.equals(value)) {
                    isSame = false;
                }
            }
        }
        return isSame;
    }

    public ArrayList<ConfigOption> getList() {
        return list;
    }
}
