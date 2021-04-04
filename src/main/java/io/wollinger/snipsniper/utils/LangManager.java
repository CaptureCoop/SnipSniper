package io.wollinger.snipsniper.utils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class LangManager {
    private static String[] langIds = {"en", "de"};
    private static HashMap<String, JSONObject> langMap = new HashMap<>();

    public static void load() {
        for(String str : langIds) {
            try {
                String content = Utils.loadFile("lang/" + str + ".json");
                langMap.put(str, new JSONObject(content));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
