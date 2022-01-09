package net.snipsniper.utils;

import net.snipsniper.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;
import net.snipsniper.utils.enums.LogLevel;

import java.util.HashMap;

public class WikiManager {
    private static final HashMap<String, JSONObject> strings = new HashMap<>();

    private WikiManager() { }

    public static void load(String language) {
        LogManager.log("Loading wiki files...", LogLevel.INFO);
        String languagesString = FileUtils.loadFileFromJar("wiki/languages.json");
        if(languagesString == null) {
            LogManager.log("Error loading languages.json!", LogLevel.ERROR);
            return;
        }

        JSONArray languages = new JSONArray(languagesString);
        String languageToLoad = "en";
        for(int i = 0; i < languages.length(); i++) {
            String langString = languages.getString(i);
            if(language.equals(langString))
                languageToLoad = langString;
        }
        finalLoad(languageToLoad);
        LogManager.log("Done!", LogLevel.INFO);
    }

    private static void finalLoad(String language) {
        String listString = FileUtils.loadFileFromJar("wiki/list.json");
        if(listString == null) {
            LogManager.log("Error loading list.json!", LogLevel.ERROR);
            return;
        }
        JSONArray list = new JSONArray(listString);
        for(int i = 0; i < list.length(); i++) {
            String string = list.getString(i);
            String json = FileUtils.loadFileFromJar(StringUtils.format("wiki/%c/" + string, language));
            if(json != null)
                strings.put(string, new JSONObject(json));
            else
                LogManager.log("Error loading json: " + string, LogLevel.ERROR);
        }
    }

    public static String getContent(String string) {
        if(!strings.containsKey(string)) {
            LogManager.log("Missing string: " + string, LogLevel.ERROR);
            return null;
        }
        return strings.get(string).getString("content");
    }
}
