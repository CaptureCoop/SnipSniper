package org.snipsniper;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class LogManager {

    private static File logFile;
    public static String htmlLog = "";
    private static final int MAX_ID_LENGTH = 10;
    private static boolean enabled = false;

    public static void log(String id, String message, Level level) {
        if(!enabled)
            return;

        String msg = "%DATETIME% [%PROFILE%]%INSERTSPACE% [%TYPE%]: %MESSAGE%";

        if(id.length() <= MAX_ID_LENGTH) {
            msg = msg.replace("%INSERTSPACE%", StringUtils.repeat(" ", MAX_ID_LENGTH - id.length()));
        } else {
            id = id.substring(0, Math.min(id.length(), MAX_ID_LENGTH));
            msg = msg.replace("%INSERTSPACE%", "");
        }

        msg = msg.replace("%PROFILE%", id);
        msg = msg.replace("%TYPE%", level.toString());
        msg = msg.replace("%MESSAGE%", message);
        final LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss:SS");
        msg = msg.replace("%DATETIME%", "" + formatter.format(time));

        System.out.println(msg);
        String color = "white";
        if(level == Level.WARNING)
            color = "yellow";
        else if(level == Level.SEVERE)
            color = "red";
        htmlLog += "<p style='margin-top:0'><font color='" + color + "'>" + escapeHtml4(msg).replaceAll(" ", "&nbsp;") + "</font></p>";

        DebugConsole console = SnipSniper.getDebugConsole();
        if(console != null)
            console.update();

        if(!SnipSniper.isDemo() && SnipSniper.getLogFolder() != null) {
            if (logFile == null) {
                LocalDateTime now = LocalDateTime.now();
                String filename = now.toString().replace(".", "_").replace(":", "_");
                filename += ".log";

                logFile = new File(SnipSniper.getLogFolder() + filename);
                try {
                    if (logFile.createNewFile())
                        LogManager.log(id, "Created new logfile at: " + logFile.getAbsolutePath(), Level.INFO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            msg += "\n";

            try {
                Files.write(Paths.get(logFile.getAbsolutePath()), msg.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setEnabled(boolean enabled) {
        LogManager.enabled = enabled;
    }

    public static File getLogFile() {
        return logFile;
    }

}
