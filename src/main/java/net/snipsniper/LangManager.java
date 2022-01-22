package net.snipsniper;

import org.capturecoop.cclogger.CCLogger;
import org.json.JSONArray;
import org.json.JSONObject;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.utils.FileUtils;
import org.capturecoop.ccutils.utils.CCStringUtils;
import org.capturecoop.cclogger.CCLogLevel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class LangManager {
    public static String DEFAULT_LANGUAGE = "en";
    public static String MISSING_STRING_CHAR = "~";
    public static ArrayList<String> languages = new ArrayList<>();
    private static final HashMap<String, JSONObject> langMap = new HashMap<>();

    private LangManager() { }

    public static void load() {
        CCLogger.log("Loading language files...");
        JSONArray langs = new JSONObject(FileUtils.loadFileFromJar("lang/languages.json")).getJSONArray("languages");
        for(int i = 0; i < langs.length(); i++) {
            String content = FileUtils.loadFileFromJar("lang/" + langs.getString(i) + ".json");
            langMap.put(langs.getString(i), new JSONObject(content));
            languages.add(langs.getString(i));
        }
        CCLogger.log("Done!");
    }

    public static JSONObject getJSON(String language) {
        return langMap.get(language);
    }

    public static String getItem(String language, String key) {
        if(langMap.get(language).getJSONObject("strings").has(key))
            return langMap.get(language).getJSONObject("strings").getString(key);
        else if (langMap.get(DEFAULT_LANGUAGE).getJSONObject("strings").has(key))
            return MISSING_STRING_CHAR + langMap.get(DEFAULT_LANGUAGE).getJSONObject("strings").getString(key);

        CCLogger.log("Could not find key <%c> in language file <%c>", CCLogLevel.ERROR, key, language);
        return "LM<" + key + ">";
    }

    public static BufferedImage getIcon(String language) {
        String file = langMap.get(language).getString("icon");
        BufferedImage flag = ImageManager.getImage(CCStringUtils.format("flags/%c.png", file));
        int size = 16;

        BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.drawImage(flag, size / 2 - flag.getWidth() / 2, size / 2 - flag.getHeight() / 2, null);
        g.dispose();
        return icon;
    }

    public static String getItem(String key) {
        return getItem(SnipSniper.getConfig().getString(ConfigHelper.MAIN.language), key);
    }

    public static String getLanguage() {
        return SnipSniper.getConfig().getString(ConfigHelper.MAIN.language);
    }

}
