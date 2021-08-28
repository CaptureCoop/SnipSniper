package org.snipsniper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.LogLevel;
import org.snipsniper.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LangManager {
    public static ArrayList<String> languages = new ArrayList<>();
    private static final HashMap<String, JSONObject> langMap = new HashMap<>();

    public static void load() {
        try {
            JSONArray langs = new JSONObject(Utils.loadFileFromJar("lang/languages.json")).getJSONArray("languages");
            for(int i = 0; i < langs.length(); i++) {
                String content = Utils.loadFileFromJar("lang/" + langs.getString(i) + ".json");
                langMap.put(langs.getString(i), new JSONObject(content));
                languages.add(langs.getString(i));
            }
        } catch (IOException e) {
            LogManager.log("Error loading languages. Message: " + e.getMessage(), LogLevel.ERROR, true);
        }
    }

    public static JSONObject getJSON(String language) {
        return langMap.get(language);
    }

    public static String getItem(String language, String key) {
        if(langMap.get(language).getJSONObject("strings").has(key))
            return langMap.get(language).getJSONObject("strings").getString(key);

        LogManager.log("Could not find key <" + key + "> in language file <" + language + ">", LogLevel.ERROR, true);
        return "LM<" + key + ">";
    }

    public static String getItem(String key) {
        return getItem(SnipSniper.getConfig().getString(ConfigHelper.MAIN.language), key);
    }

}
