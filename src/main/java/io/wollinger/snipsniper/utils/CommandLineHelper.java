package io.wollinger.snipsniper.utils;

import io.wollinger.snipsniper.Main;

public class CommandLineHelper {

    public CommandLineHelper() {

    }

    public void handle(String[] args) {
        if(args.length == 0)
            return;

        switch(args[0]) {
            case "-help":
            case "-?":
                helpText();
                System.exit(0);
            case "-version":
            case "-v":
                System.out.println(Main.VERSION);
                System.exit(0);
            case "-demo":
                Main.isDemo = true;
                break;
        }
    }

    public void helpText() {
        System.out.println("-help / -?    = Displays this");
        System.out.println("-version / -v = Displays version");
        System.out.println("-demo         = Starts SnipSniper in demo mode (No configs are beeing created)");
    }

}
