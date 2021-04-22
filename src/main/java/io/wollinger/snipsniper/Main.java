package io.wollinger.snipsniper;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.CommandLineHelper;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.LangManager;
import io.wollinger.snipsniper.utils.LogManager;
import org.apache.commons.lang3.SystemUtils;

public class Main {
	
	public final static String VERSION = "20210422_4";
	
	public static String jarFolder = new File("").getAbsolutePath() + "/";
	public static String mainFolder = jarFolder + "SnipSniper";
	public static String profilesFolder = mainFolder + "/Profiles/";
	public static String logFolder = mainFolder + "/logs/";
	
	public final static int PROFILE_COUNT = 7;
	
	public static Sniper[] profiles = new Sniper[PROFILE_COUNT];

	public static boolean isIdle = true;

	public static boolean isDemo = false;

	public static File logFile = null;

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		new CommandLineHelper().handle(args);

		LogManager.log("Main", "Launching SnipSniper Version " + Main.VERSION, Level.INFO);

		if(SystemUtils.IS_OS_WINDOWS)
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		LangManager.load();

		System.setProperty("sun.java2d.uiScale", "1.0");

		LogManager.log("Main", "Loading resources", Level.INFO);
		Icons.loadResources();

		if(!isDemo) {
			new File(profilesFolder).mkdirs();
			new File(logFolder).mkdirs();
		} else {
			LogManager.log("Main", "========================================================", Level.INFO);
			LogManager.log("Main", "SnipSniper is running in DEMO mode", Level.INFO);
			LogManager.log("Main", "This means that no files will be created and/or modified", Level.INFO);
			LogManager.log("Main", "========================================================", Level.INFO);
		}

		if(SystemUtils.IS_OS_WINDOWS) {
			new Sniper(0).cfg.save();
			for (int i = 0; i < PROFILE_COUNT; i++) {
				if (new File(profilesFolder + "profile" + (i + 1) + ".txt").exists()) {
					profiles[i] = new Sniper(i + 1);
				}
			}
		}
	}

	public static void exit() {
		LogManager.log("Main", "Exit requested. Goodbye!", Level.INFO);
		System.exit(0);
	}

}
