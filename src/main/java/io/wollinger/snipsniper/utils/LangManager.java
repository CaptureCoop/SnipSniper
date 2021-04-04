package io.wollinger.snipsniper.utils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class LangManager {
    private static final String[] langIds = {"en", "de"};
    private static HashMap<String, JSONObject> langMap = new HashMap<>();

    public static void load() {
        for(String str : langIds) {
            try {
                String content = Utils.loadFileFromJar("lang/" + str + ".json");
                langMap.put(str, new JSONObject(content));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getItem(String key, String language) {
        return langMap.get(language).getJSONObject("strings").getString(key);
    }

}
