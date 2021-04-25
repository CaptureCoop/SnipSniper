package io.wollinger.snipsniper;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;

import javax.swing.*;

import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.*;
import org.apache.commons.lang3.SystemUtils;

public class SnipSniper {

	public final static String VERSION = "20210423_3";
	
	public static String jarFolder = new File("").getAbsolutePath() + "/";
	public static String mainFolder = jarFolder + "SnipSniper";
	public static String profilesFolder = mainFolder + "/cfg/";
	public static String logFolder = mainFolder + "/logs/";
	
	public final static int PROFILE_COUNT = 7;

	public static Sniper mainProfile;
	public static Sniper[] profiles = new Sniper[PROFILE_COUNT];

	public static boolean isIdle = true;

	public static boolean isDemo = false;

	public static File logFile = null;

	public static Config config;

	public static String[] args;

	public SnipSniper(String[] args, boolean saveInDocuments, boolean isDebug) {
		if(!SystemUtils.IS_OS_WINDOWS)
			System.out.println("SnipSniper is currently only available for Windows. Sorry!");

		CommandLineHelper cmdline = new CommandLineHelper();
		cmdline.handle(args);
		SnipSniper.args = args;

		if(saveInDocuments)
			SnipSniper.setSaveLocationToDocuments();

		if(!isDemo) {
			File tempProfileFolder = new File(profilesFolder);
			File tempLogFolder = new File(logFolder);
			if ((!tempProfileFolder.exists() && !tempProfileFolder.mkdirs()) || (!tempLogFolder.exists() && !tempLogFolder.mkdirs())){
				LogManager.log("Main", "Could not create required folders! Exiting...", Level.SEVERE);
				exit();
			}
		}

		config = new Config("main.cfg", "cfgM", "main_defaults.cfg");
		String language = cmdline.getLanguage();
		if(language != null && !language.isEmpty())
			config.set("language", language);

		LangManager.load();

		LogManager.log("Main", "Launching SnipSniper Version " + SnipSniper.VERSION, Level.INFO);

		if(isDebug || cmdline.isDebug()) {
			LogManager.log("Main", "========================================", Level.INFO);
			LogManager.log("Main", "= SnipSniper is running in debug mode! =", Level.INFO);
			LogManager.log("Main", "========================================", Level.INFO);
		}

		LogManager.log("Main", "Launching language <" + SnipSniper.config.getString("language") + "> using encoding <" + Charset.defaultCharset() + ">!", Level.INFO);

		if(!LangManager.languages.contains(SnipSniper.config.getString("language"))) {
			LogManager.log("Main", "Language <" + SnipSniper.config.getString("language") + "> not found. Available languages: " + LangManager.languages.toString(), Level.SEVERE);
			exit();
		}

		String wantedEncoding = SnipSniper.config.getString("encoding");
		if(SnipSniper.config.getBool("enforceEncoding") && !Charset.defaultCharset().toString().equals(wantedEncoding)) {
			if(!Charset.availableCharsets().containsKey(wantedEncoding)) {
				LogManager.log("Main", "Charset \"" + wantedEncoding + "\" missing. Language \"" + SnipSniper.config.getString("language")+ "\" not available", Level.SEVERE);
				JOptionPane.showMessageDialog(null, Utils.formatArgs(LangManager.getItem(LangManager.languages.get(0), "error_charset_not_available"), wantedEncoding, SnipSniper.config.getString("language")));
				exit();
			}
			LogManager.log("Main", "Charset <" + wantedEncoding + "> needed! Restarting with correct charset...", Level.WARNING);
			try {
				if(cmdline.isRestartedInstance()) {
					JOptionPane.showMessageDialog(null, "Charset change failed. Please try using a different Java Version! (Java 1.8 / 8)");
					exit();
				}
				SnipSniper.restartApplication(wantedEncoding);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}

		config.save();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		System.setProperty("sun.java2d.uiScale", "1.0");

		LogManager.log("Main", "Loading resources", Level.INFO);
		Icons.loadResources();

		if(isDemo) {
			LogManager.log("Main", "============================================================", Level.INFO);
			LogManager.log("Main", "= SnipSniper is running in DEMO mode                       =", Level.INFO);
			LogManager.log("Main", "= This means that no files will be created and/or modified =", Level.INFO);
			LogManager.log("Main", "============================================================", Level.INFO);
		}

		resetProfiles();

	}

	public static void resetProfiles() {
		SystemTray tray = SystemTray.getSystemTray();
		for(TrayIcon icon : tray.getTrayIcons()) {
			tray.remove(icon);
		}

		mainProfile = null;
		Arrays.fill(profiles, null);

		mainProfile = new Sniper(0);
		mainProfile.cfg.save();
		for (int i = 0; i < PROFILE_COUNT; i++) {
			if (new File(profilesFolder + "profile" + (i + 1) + ".cfg").exists()) {
				profiles[i] = new Sniper(i + 1);
			}
		}
	}

	public static void setSaveLocationToDocuments() {
		SnipSniper.jarFolder = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
		SnipSniper.mainFolder = jarFolder + "/SnipSniper";
		SnipSniper.profilesFolder = mainFolder + "/cfg/";
		SnipSniper.logFolder = mainFolder + "/logs/";
	}

	//https://stackoverflow.com/questions/4159802/how-can-i-restart-a-java-application
	public static void restartApplication(String encoding) throws URISyntaxException, IOException {
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final File currentJar = new File(SnipSniper.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		if(!currentJar.getName().endsWith(".jar"))
			return;

		final ArrayList<String> command = new ArrayList<>();
		command.add(javaBin);
		if(encoding != null && !encoding.isEmpty())
			command.add("-Dfile.encoding=" + encoding);
		command.add("-jar");
		command.add(currentJar.getPath());
		command.add("-r");
		Collections.addAll(command, SnipSniper.args);

		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.start();
		exit();
	}

	public static void exit() {
		LogManager.log("Main", "Exit requested. Goodbye!", Level.INFO);
		System.exit(0);
	}

}
