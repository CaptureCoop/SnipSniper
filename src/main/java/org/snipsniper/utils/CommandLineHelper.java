package org.snipsniper.utils;

import org.snipsniper.SnipSniper;
import org.snipsniper.config.ConfigHelper;

public class CommandLineHelper {
    private String language;
    private boolean isRestartedInstance = false;
    private boolean isDebug = false;
    private boolean editorOnly = false;
    private String editorFile;
    private boolean viewerOnly = false;
    private String viewerFile;
    private String platform;

    public CommandLineHelper() {

    }

    public void handle(String[] args) {
        if(args.length == 0)
            return;

        boolean doExit = false;

        int index = 0;
        for(String arg : args) {
            switch(arg) {
                case "-help":
                case "-?":
                    helpText();
                    doExit = true;
                    break;
                case "-version":
                case "-v":
                    System.out.println(SnipSniper.getVersion() + "-" + SnipSniper.getReleaseType().toString().toLowerCase() + " rev-" + SnipSniper.BUILDINFO.getString(ConfigHelper.BUILDINFO.githash));
                    doExit = true;
                    break;
                case "-demo":
                    SnipSniper.setDemo(true);
                    break;
                case "-language":
                case "-lang":
                case "-l":
                    if(args.length > index + 1) language = args[index + 1];
                    else System.out.println("Missing argument after " + arg + "!");
                    break;
                case "-r":
                    isRestartedInstance = true;
                    break;
                case "-d":
                case "-debug":
                    isDebug = true;
                    break;
                case "-editor":
                    editorOnly = true;
                    if(args.length > index + 1) editorFile = args[index + 1];
                    break;
                case "-viewer":
                    viewerOnly = true;
                    if(args.length > index + 1) viewerFile = args[index + 1];
                    break;
                case "-debugLang":
                    Utils.jsonLang();
                    doExit = true;
                    break;
                case "-platform":
                    if(args.length > index + 1) platform = args[index + 1];
                    else System.out.println("Missing argument after " + arg + "!");
                    break;

            }
            index++;
        }

        if(doExit)
            System.exit(0);
    }

    public void helpText() {
        System.out.println("-help / -?     = Displays this");
        System.out.println("-version / -v  = Displays version");
        System.out.println("-demo          = Starts SnipSniper in demo mode (No configs are being created)");
        System.out.println("-language / -l = Sets the language. Useful for demo mode");
    }

    public String getLanguage() {
        return language;
    }

    public boolean isRestartedInstance() {
        return isRestartedInstance;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public boolean isEditorOnly() {
        return editorOnly;
    }

    public String getEditorFile() {
        return editorFile;
    }

    public boolean isViewerOnly() {
        return viewerOnly;
    }

    public String getViewerFile() {
        return viewerFile;
    }

    public String getPlatform() {
        return platform;
    }

}
