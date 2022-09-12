package net.snipsniper.utils.debug;

import net.snipsniper.LangManager;
import net.snipsniper.utils.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

public class DebugUtils {
    public static void jsonLang() {
        System.out.println("Creating language debug files under \\lang\\...\n");
        if(!new File("lang").mkdir() && !new File("lang").exists()) {
            System.out.println("Could not create folder. Aborting");
            return;
        }
        LangManager.Companion.load();

        JSONObject en = LangManager.Companion.getJSON("en");
        FileUtils.printFile("lang/en.json", en.toString());

        for(String language : LangManager.Companion.getLanguages()) {
            boolean successful = true;
            JSONObject strings = en.getJSONObject("strings");
            Iterator<String> keys = strings.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                JSONObject obj = LangManager.Companion.getJSON(language).getJSONObject("strings");
                if(!obj.has(key)) {
                    successful = false;
                    System.out.println(language + ".json is missing <" + key + ">");
                    obj.put(key, "<MISSING>");
                }
            }
            if(!successful) {
                FileUtils.printFile("lang/" + language + ".json", LangManager.Companion.getJSON(language).toString());
                System.out.println("Missing lines found for " + language + ".json\n");
            }
        }
        System.out.println("Done. If no issues were reported you are golden :^)");
    }
}
