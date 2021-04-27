package io.wollinger.snipsniper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.*;

import io.wollinger.snipsniper.editorwindow.EditorWindow;
import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.*;
import org.apache.commons.lang3.SystemUtils;

public class SnipSniper {

	public static String VERSION;
	
	public static String jarFolder;
	public static String mainFolder;
	public static String profilesFolder;
	public static String logFolder;
	
	public final static int PROFILE_COUNT = 7;

	public static Sniper mainProfile;
	public static Sniper[] profiles = new Sniper[PROFILE_COUNT];

	public static boolean isIdle = true;

	public static boolean isDemo = false;

	public static File logFile = null;

	public static Config config;

	public static String[] args;

	private final static String ID = "MAIN";

	public SnipSniper(String[] args, boolean saveInDocuments, boolean isDebug, boolean isEditorOnly) {
		if(!SystemUtils.IS_OS_WINDOWS)
			System.out.println("SnipSniper is currently only available for Windows. Sorry!");

		try {
			SnipSniper.VERSION = Utils.loadFileFromJar("version.txt") + ".";
			SnipSniper.VERSION += Utils.loadFileFromJar("build.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		CommandLineHelper cmdline = new CommandLineHelper();
		cmdline.handle(args);
		SnipSniper.args = args;

		if(saveInDocuments)
			SnipSniper.setSaveLocationToDocuments();
		else
			setSaveLocationToJar();

		if(!isDemo) {
			File tempProfileFolder = new File(profilesFolder);
			File tempLogFolder = new File(logFolder);
			if ((!tempProfileFolder.exists() && !tempProfileFolder.mkdirs()) || (!tempLogFolder.exists() && !tempLogFolder.mkdirs())){
				LogManager.log(ID, "Could not create required folders! Exiting...", Level.SEVERE);
				exit();
			}
		}

		config = new Config("main.cfg", "CFGM", "main_defaults.cfg");
		String language = cmdline.getLanguage();
		if(language != null && !language.isEmpty())
			config.set("language", language);

		LangManager.load();

		LogManager.log(ID, "Launching SnipSniper Version " + SnipSniper.VERSION, Level.INFO);

		if(isDebug || cmdline.isDebug()) {
			LogManager.log(ID, "========================================", Level.INFO);
			LogManager.log(ID, "= SnipSniper is running in debug mode! =", Level.INFO);
			LogManager.log(ID, "========================================", Level.INFO);
		}

		LogManager.log(ID, "Launching language <" + SnipSniper.config.getString("language") + "> using encoding <" + Charset.defaultCharset() + ">!", Level.INFO);

		if(!LangManager.languages.contains(SnipSniper.config.getString("language"))) {
			LogManager.log(ID, "Language <" + SnipSniper.config.getString("language") + "> not found. Available languages: " + LangManager.languages.toString(), Level.SEVERE);
			exit();
		}

		String wantedEncoding = SnipSniper.config.getString("encoding");
		if(SnipSniper.config.getBool("enforceEncoding") && !Charset.defaultCharset().toString().equals(wantedEncoding)) {
			if(!Charset.availableCharsets().containsKey(wantedEncoding)) {
				LogManager.log(ID, "Charset \"" + wantedEncoding + "\" missing. Language \"" + SnipSniper.config.getString("language")+ "\" not available", Level.SEVERE);
				JOptionPane.showMessageDialog(null, Utils.formatArgs(LangManager.getItem(LangManager.languages.get(0), "error_charset_not_available"), wantedEncoding, SnipSniper.config.getString("language")));
				exit();
			}
			LogManager.log(ID, "Charset <" + wantedEncoding + "> needed! Restarting with correct charset...", Level.WARNING);
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

		LogManager.log(ID, "Loading resources", Level.INFO);
		Icons.loadResources();

		if(isDemo) {
			LogManager.log(ID, "============================================================", Level.INFO);
			LogManager.log(ID, "= SnipSniper is running in DEMO mode                       =", Level.INFO);
			LogManager.log(ID, "= This means that no files will be created and/or modified =", Level.INFO);
			LogManager.log(ID, "============================================================", Level.INFO);
		}

		if(cmdline.isEditorOnly() || isEditorOnly) {
			Config config = new Config("editor.cfg", "CFGE", "profile_defaults.cfg");
			config.save();

			boolean fileExists = true;
			BufferedImage img = null;
			if((cmdline.getEditorFile() != null && !cmdline.getEditorFile().isEmpty()) || (isEditorOnly && args.length > 0)) {
				try {
					String path;
					if(cmdline.getEditorFile() != null && !cmdline.getEditorFile().isEmpty())
						path = cmdline.getEditorFile();
					else
						path = args[0];

					File file = new File(path);
					fileExists = file.exists();
					if(fileExists)
						img = ImageIO.read(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				img = new BufferedImage(512, 256, BufferedImage.TYPE_INT_RGB);
			}

			if(img != null && fileExists) {
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				int x = (int) (screenSize.getWidth() / 2 - img.getWidth() / 2);
				int y = (int) (screenSize.getHeight() / 2 - img.getHeight() / 2);
				new EditorWindow("EDIT", img, x, y, "SnipSniper Editor", config, false, "", false);
				//TODO: it exits fine when closed, check if this always happens
			} else {
				JOptionPane.showMessageDialog(null, "Image not found!", "Error", 0);
			}
		} else {
			resetProfiles();
		}
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
		SnipSniper.jarFolder = System.getProperty("user.home");
		SnipSniper.mainFolder = jarFolder + "/.SnipSniper";
		SnipSniper.profilesFolder = mainFolder + "/cfg/";
		SnipSniper.logFolder = mainFolder + "/logs/";
	}

	public static void setSaveLocationToJar() {
		String folderToUseString = SnipSniper.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		File folderToUse = new File(folderToUseString);
		if(folderToUse.getName().endsWith(".jar"))
			jarFolder = folderToUseString.replace(folderToUse.getName(), "");
		else
			jarFolder = folderToUseString;

		mainFolder = jarFolder + "SnipSniper";
		profilesFolder = mainFolder + "/cfg/";
		logFolder = mainFolder + "/logs/";
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
		LogManager.log(ID, "Exit requested. Goodbye!", Level.INFO);
		System.exit(0);
	}

}
