package net.snipsniper.utils;

import org.capturecoop.cclogger.CCLogger;
import org.capturecoop.ccutils.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.capturecoop.cclogger.LogLevel;

import java.util.HashMap;

public class WikiManager {
    private static final HashMap<String, JSONObject> strings = new HashMap<>();

    private WikiManager() { }

    public static void load(String language) {
        CCLogger.log("Loading wiki files...", LogLevel.INFO);
        String languagesString = FileUtils.loadFileFromJar("wiki/languages.json");
        if(languagesString == null) {
            CCLogger.log("Error loading languages.json!", LogLevel.ERROR);
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
        CCLogger.log("Done!", LogLevel.INFO);
    }

    private static void finalLoad(String language) {
        String listString = FileUtils.loadFileFromJar("wiki/list.json");
        if(listString == null) {
            CCLogger.log("Error loading list.json!", LogLevel.ERROR);
            return;
        }
        JSONArray list = new JSONArray(listString);
        for(int i = 0; i < list.length(); i++) {
            String string = list.getString(i);
            String json = FileUtils.loadFileFromJar(StringUtils.format("wiki/%c/" + string, language));
            if(json != null)
                strings.put(string, new JSONObject(json));
            else
                CCLogger.log("Error loading json: " + string, LogLevel.ERROR);
        }
    }

    public static String getContent(String string) {
        if(!strings.containsKey(string)) {
            CCLogger.log("Missing string: " + string, LogLevel.ERROR);
            return null;
        }
        return strings.get(string).getString("content");
    }
}
