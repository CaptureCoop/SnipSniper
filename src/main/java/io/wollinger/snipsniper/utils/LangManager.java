package io.wollinger.snipsniper.utils;

import io.wollinger.snipsniper.Main;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

public class LangManager {
    private static final HashMap<String, JSONObject> langMap = new HashMap<>();

    public static void load() {
        try {
            JSONArray languages = new JSONObject(Utils.loadFileFromJar("lang/languages.json")).getJSONArray("languages");
            for(int i = 0; i < languages.length(); i++) {
                String content = Utils.loadFileFromJar("lang/" + languages.getString(i) + ".json");
                langMap.put(languages.getString(i), new JSONObject(content));
            }
        } catch (IOException e) {
            e.printStackTrace();
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
