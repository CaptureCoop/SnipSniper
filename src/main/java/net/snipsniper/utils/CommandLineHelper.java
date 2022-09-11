package net.snipsniper.utils;

import net.snipsniper.SnipSniper;
import net.snipsniper.utils.debug.DebugUtils;

public class CommandLineHelper {
    private String language;
    private boolean isRestartedInstance = false;
    private boolean isDebug = false;
    private boolean editorOnly = false;
    private String editorFile;
    private boolean viewerOnly = false;
    private String viewerFile;

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
                    System.out.println(SnipSniper.Companion.getVersion());
                    doExit = true;
                    break;
                case "-demo":
                    SnipSniper.Companion.setDemo(true);
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
                    DebugUtils.jsonLang();
                    doExit = true;
                    break;
                default:
                    System.out.println("Unrecognized argument <" + arg + ">. Use argument -help to see all the commands!");
                    break;
            }
            index++;
        }

        if(doExit)
            System.exit(0);
    }

    public void helpText() {
        System.out.println("SnipSniper " + SnipSniper.Companion.getVersion() + "\n");
        System.out.println("General commands:");
        System.out.println("-help / -?     = Displays this");
        System.out.println("-version / -v  = Displays version");
        System.out.println("-demo          = Starts SnipSniper in demo mode (No configs are being created)");
        System.out.println("-language / -l = Sets the language. Useful for demo mode");
        System.out.println("-editor        = Starts the standalone editor (You can enter a path after -editor)");
        System.out.println("-viewer        = Starts the standalone viewer (You can enter a path after -viewer)");
        System.out.println("\nDebug Commands:");
        System.out.println("-debug         = Starts SnipSniper in Debug mode");
        System.out.println("-debugLang     = Test language files for missing strings");
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

}
