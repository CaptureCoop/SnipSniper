package org.snipsniper;

import org.snipsniper.utils.enums.LogLevel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class FontManager {
    private static Font atkinsonRegular;

    public static void loadFonts() {
        InputStream is = FontManager.class.getResourceAsStream("/org/snipsniper/resources/fonts/atkinson/Atkinson-Hyperlegible-Regular-102.ttf");
        Font tempFont = null;
        try {
            tempFont = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        atkinsonRegular = tempFont.deriveFont(12F);

        LogManager.log(atkinsonRegular.toString(), LogLevel.INFO);

        setUIFont(atkinsonRegular);
    }

    public static void setUIFont (Font f){
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }


}
