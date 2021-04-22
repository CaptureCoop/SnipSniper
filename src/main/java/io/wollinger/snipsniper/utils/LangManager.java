package io.wollinger.snipsniper.utils;

import io.wollinger.snipsniper.Main;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

public class LangManager {
    private static final String[] langIds = {"en", "de"};
    private static final HashMap<String, JSONObject> langMap = new HashMap<>();

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

    public static String getItem(String key) {
        String language = Main.config.getString("language");
        if(langMap.get(language).getJSONObject("strings").has(key))
            return langMap.get(language).getJSONObject("strings").getString(key);

        LogManager.log("lang", "Could not find key <" + key + "> in language file <" + language + ">", Level.SEVERE);
        return "null";
    }

}
