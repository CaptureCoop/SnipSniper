package org.snipsniper.utils.debug;

import org.json.JSONObject;
import org.snipsniper.LangManager;
import org.snipsniper.utils.FileUtils;

import java.io.File;
import java.util.Iterator;

public class DebugUtils {
    public static void jsonLang() {
        System.out.println("Creating language debug files under \\lang\\...\n");
        if(!new File("lang").mkdir() && !new File("lang").exists()) {
            System.out.println("Could not create folder. Aborting");
            return;
        }
        LangManager.load();

        JSONObject en = LangManager.getJSON("en");
        FileUtils.printFile("lang/en.json", en.toString());

        for(String language : LangManager.languages) {
            boolean successful = true;
            JSONObject strings = en.getJSONObject("strings");
            Iterator<String> keys = strings.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                JSONObject obj = LangManager.getJSON(language).getJSONObject("strings");
                if(!obj.has(key)) {
                    successful = false;
                    System.out.println(language + ".json is missing <" + key + ">");
                    obj.put(key, "<MISSING>");
                }
            }
            if(!successful) {
                FileUtils.printFile("lang/" + language + ".json", LangManager.getJSON(language).toString());
                System.out.println("Missing lines found for " + language + ".json\n");
            }
        }
        System.out.println("Done. If no issues were reported you are golden :^)");
    }
}
