package org.snipsniper;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import org.apache.commons.lang3.StringUtils;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.LogLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogManager {

    private static File logFile;
    public static String htmlLog = "";
    private static final int MAX_LEVEL_LENGTH = LogLevel.WARNING.toString().length();
    private static boolean enabled = false;

    public static void log(String message, LogLevel level) {
        logInternal(message, level, false);
    }

    public static void log(String message, LogLevel level, boolean printStackTrace) {
        logInternal(message, level, printStackTrace);
    }

    //The reason for this is that this way we can take index 3 of stack trace at all times
    private static void logInternal(String message, LogLevel level, boolean printStackTrace) {
        if(!enabled)
            return;

        StringBuilder msg = new StringBuilder("%DATETIME% [%TYPE%]%INSERTSPACE% [%CLASS%]: %MESSAGE%");

        String levelString = level.toString();

        if(levelString.length() <= MAX_LEVEL_LENGTH) {
            msg = new StringBuilder(msg.toString().replace("%INSERTSPACE%", StringUtils.repeat(" ", MAX_LEVEL_LENGTH - levelString.length())));
        } else {
            levelString = levelString.substring(0, Math.min(levelString.length(), MAX_LEVEL_LENGTH));
            msg = new StringBuilder(msg.toString().replace("%INSERTSPACE%", ""));
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final int STACKTRACE_START = 3;
        StackTraceElement currentStackTrace = stackTrace[STACKTRACE_START];

        msg = new StringBuilder(msg.toString().replace("%CLASS%", currentStackTrace.getClassName() + "." + currentStackTrace.getMethodName() + ":" + currentStackTrace.getLineNumber()));
        msg = new StringBuilder(msg.toString().replace("%INSERTSPACE%", ""));
        msg = new StringBuilder(msg.toString().replace("%TYPE%", levelString));
        msg = new StringBuilder(msg.toString().replace("%MESSAGE%", message));
        final LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss:SS");
        String dateTimeString = formatter.format(time) + "";
        msg = new StringBuilder(msg.toString().replace("%DATETIME%", dateTimeString));

        if(printStackTrace) {
            int stackSizingHelp = dateTimeString.length() + MAX_LEVEL_LENGTH + 4;

            msg.append("%NEWLINE%");
            for(int i = STACKTRACE_START; i < stackTrace.length; i++) {
                String trace = stackTrace[i].toString();
                if(trace.contains("org.snipsniper"))
                    msg.append(StringUtils.repeat(" ", stackSizingHelp)).append("[").append(trace).append("]%NEWLINE%");
            }
            msg.append("%NEWLINE%");
        }

        System.out.println(msg);
        String color = "white";
        if(level == LogLevel.WARNING)
            color = "yellow";
        else if(level == LogLevel.ERROR)
            color = "red";

        String finalMsg = escapeHtml4(msg.toString()).replaceAll(" ", "&nbsp;");
        finalMsg = finalMsg.replaceAll("%NEWLINE%", "<br>");
        if(SnipSniper.getConfig() != null && finalMsg.contains("org.snipsniper")) {
            String baseTreeLink = "https://github.com/SvenWollinger/SnipSniper/tree/" + SnipSniper.BUILDINFO.getString(ConfigHelper.BUILDINFO.githash) + "/src/main/java/";
            String link = baseTreeLink + currentStackTrace.getClassName().replaceAll("\\.", "/") + ".java#L" + currentStackTrace.getLineNumber();
            finalMsg = finalMsg.replace(":" + currentStackTrace.getLineNumber() + "]", ":" + currentStackTrace.getLineNumber() + " <a href='" + link + "'>@</a>]");
        }
        htmlLog += "<p style='margin-top:0; white-space: nowrap;'><font color='" + color + "'>" + finalMsg + "</font></p>";

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
                        LogManager.log("Created new logfile at: " + logFile.getAbsolutePath(), LogLevel.INFO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            msg.append("\n");

            try {
                Files.write(Paths.get(logFile.getAbsolutePath()), msg.toString().getBytes(), StandardOpenOption.APPEND);
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
