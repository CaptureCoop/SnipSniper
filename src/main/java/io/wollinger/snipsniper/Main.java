package io.wollinger.snipsniper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

import javax.swing.*;

import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.*;
import org.apache.commons.lang3.SystemUtils;

public class Main {

	public final static String VERSION = "20210423_3";
	
	public static String jarFolder = new File("").getAbsolutePath() + "/";
	public static String mainFolder = jarFolder + "SnipSniper";
	public static String profilesFolder = mainFolder + "/cfg/";
	public static String logFolder = mainFolder + "/logs/";
	
	public final static int PROFILE_COUNT = 7;
	
	public static Sniper[] profiles = new Sniper[PROFILE_COUNT];

	public static boolean isIdle = true;

	public static boolean isDemo = false;

	public static File logFile = null;

	public static Config config;

	public static String[] args;

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		if(!SystemUtils.IS_OS_WINDOWS)
			System.out.println("SnipSniper is currently only available for Windows. Sorry!");

		CommandLineHelper cmdline = new CommandLineHelper();
		cmdline.handle(args);
		Main.args = args;

		if(!isDemo) {
			if (!new File(profilesFolder).mkdirs() || !new File(logFolder).mkdirs()){
				LogManager.log("Main", "Could not create required folders! Exiting...", Level.SEVERE);
				exit();
			}
		}

		config = new Config("main.cfg", "cfgM", "main_defaults.cfg");
		String language = cmdline.getLanguage();
		if(language != null && !language.isEmpty())
			config.set("language", language);

		LangManager.load();

		LogManager.log("Main", "Launching SnipSniper Version " + Main.VERSION, Level.INFO);
		LogManager.log("Main", "Launching language <" + Main.config.getString("language") + "> using encoding <" + Charset.defaultCharset() + ">!", Level.INFO);

		if(!LangManager.languages.contains(Main.config.getString("language"))) {
			LogManager.log("Main", "Language <" + Main.config.getString("language") + "> not found. Available languages: " + LangManager.languages.toString(), Level.SEVERE);
			exit();
		}

		if(!LangManager.getEncoding().equals("none") && Main.config.getBool("enforceEncoding") && !Charset.defaultCharset().toString().equals(LangManager.getEncoding())) {
			if(!Charset.availableCharsets().containsKey(LangManager.getEncoding())) {
				LogManager.log("Main", "Charset \"" + LangManager.getEncoding() + "\" missing. Language \"" + Main.config.getString("language")+ "\" not available", Level.SEVERE);
				JOptionPane.showMessageDialog(null, Utils.formatArgs(LangManager.getItem(LangManager.languages.get(0), "error_charset_not_available"), LangManager.getEncoding(), Main.config.getString("language")));
				exit();
			}
			LogManager.log("Main", "Charset <" + LangManager.getEncoding() + "> needed! Restarting with correct charset...", Level.WARNING);
			try {
				if(cmdline.isRestartedInstance()) {
					JOptionPane.showMessageDialog(null, "Charset change failed. Please try using a different Java Version! (Java 1.8 / 8)");
					exit();
				}
				Main.restartApplication();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}

		config.save();

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		System.setProperty("sun.java2d.uiScale", "1.0");

		LogManager.log("Main", "Loading resources", Level.INFO);
		Icons.loadResources();

		if(isDemo) {
			LogManager.log("Main", "========================================================", Level.INFO);
			LogManager.log("Main", "SnipSniper is running in DEMO mode", Level.INFO);
			LogManager.log("Main", "This means that no files will be created and/or modified", Level.INFO);
			LogManager.log("Main", "========================================================", Level.INFO);
		}
		new Sniper(0).cfg.save();
		for (int i = 0; i < PROFILE_COUNT; i++) {
			if (new File(profilesFolder + "profile" + (i + 1) + ".cfg").exists()) {
				profiles[i] = new Sniper(i + 1);
			}
		}
	}

	//https://stackoverflow.com/questions/4159802/how-can-i-restart-a-java-application
	public static void restartApplication() throws URISyntaxException, IOException {
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		if(!currentJar.getName().endsWith(".jar"))
			return;

		final ArrayList<String> command = new ArrayList<>();
		command.add(javaBin);
		if(!LangManager.getEncoding().equals("none"))
			command.add("-Dfile.encoding=" + LangManager.getEncoding());
		command.add("-jar");
		command.add(currentJar.getPath());
		command.add("-r");
		Collections.addAll(command, Main.args);

		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.start();
		exit();
	}

	public static void exit() {
		LogManager.log("Main", "Exit requested. Goodbye!", Level.INFO);
		System.exit(0);
	}

}
