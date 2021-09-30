package org.snipsniper.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.snipsniper.LangManager;
import org.snipsniper.LogManager;
import org.snipsniper.utils.enums.LogLevel;

import java.io.IOException;
import java.util.HashMap;

public class WikiManager {
    private static HashMap<String, JSONObject> strings = new HashMap<>();

    private WikiManager() { }

    public static void load(String language) {
        LogManager.log("Loading wiki files...", LogLevel.INFO);
        JSONArray languages = new JSONArray(Utils.loadFileFromJar("wiki/languages.json"));

        String languageToLoad = "en";

        for(int i = 0; i < languages.length(); i++) {
            if(language.equals(languages.getString(i)))
                languageToLoad = languages.getString(i);
        }
        finalLoad(languageToLoad);
        LogManager.log("Done!", LogLevel.INFO);
    }

    private static void finalLoad(String language) {
        JSONArray list = new JSONArray(Utils.loadFileFromJar("wiki/list.json"));
        for(int i = 0; i < list.length(); i++) {
            String string = list.getString(i);
            strings.put(string, new JSONObject(Utils.loadFileFromJar(StringUtils.format("wiki/%c/" + string, language))));
        }
    }
}
