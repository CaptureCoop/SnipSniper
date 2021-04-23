package io.wollinger.snipsniper.utils;

import io.wollinger.snipsniper.Main;

public class CommandLineHelper {

    private String language;
    private boolean isRestartedInstance = false;

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
                    System.out.println(Main.VERSION);
                    doExit = true;
                    break;
                case "-demo":
                    Main.isDemo = true;
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
            }
            index++;
        }

        if(doExit)
            System.exit(0);
    }

    public void helpText() {
        System.out.println("-help / -?     = Displays this");
        System.out.println("-version / -v  = Displays version");
        System.out.println("-demo          = Starts SnipSniper in demo mode (No configs are beeing created)");
        System.out.println("-language / -l = Sets the language. Useful for demo mode");
    }

    public String getLanguage() {
        return language;
    }

    public boolean isRestartedInstance() {
        return isRestartedInstance;
    }

}
