package io.wollinger.snipsniper.utils;

import io.wollinger.snipsniper.Main;

public class CommandLineHelper {

    public CommandLineHelper() {

    }

    public void handle(String[] args) {
        if(args.length == 0)
            return;

        boolean doExit = false;

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
            }
        }

        if(doExit)
            System.exit(0);
    }

    public void helpText() {
        System.out.println("-help / -?    = Displays this");
        System.out.println("-version / -v = Displays version");
        System.out.println("-demo         = Starts SnipSniper in demo mode (No configs are beeing created)");
    }

}
