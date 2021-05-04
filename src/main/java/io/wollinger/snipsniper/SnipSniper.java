package io.wollinger.snipsniper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.*;

import io.wollinger.snipsniper.editorwindow.EditorWindow;
import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.*;
import io.wollinger.snipsniper.viewer.ViewerWindow;
import org.apache.commons.lang3.SystemUtils;

public final class SnipSniper {

	private static String VERSION;
	
	private static String jarFolder;
	private static String mainFolder;
	private static String profilesFolder;
	private static String logFolder;

	private final static int PROFILE_COUNT = 7;

	private static final Sniper[] profiles = new Sniper[PROFILE_COUNT];

	private static boolean isIdle = true;

	private static boolean isDemo = false;

	private static Config config;

	private final static String ID = "MAIN";

	public static void start(String[] args, boolean saveInDocuments, boolean isDebug, boolean isEditorOnly) {
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
				if(!Utils.restartApplication(wantedEncoding, args))
					LogManager.log(ID, "Restart failed. Possibly not running in jar. Starting with charset <" + Charset.defaultCharset() + ">", Level.WARNING);
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
			Config config =  EditorWindow.getStandaloneEditorConfig();
			config.save();

			boolean fileExists;
			BufferedImage img = null;
			String path = "";
			if((cmdline.getEditorFile() != null && !cmdline.getEditorFile().isEmpty()) || (isEditorOnly && args.length > 0)) {
				try {
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
			}

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int width = 512;
			int height = 512;
			if(img != null) {
				width = img.getWidth();
				height = img.getHeight();
			}
			int x = (int) (screenSize.getWidth() / 2 - width / 2);
			int y = (int) (screenSize.getHeight() / 2 - height / 2);
			new EditorWindow("EDIT", img, x, y, "SnipSniper Editor", config, false, path, false, true);
		} else if(cmdline.isViewerOnly()) {
			File file = null;
			if(cmdline.getViewerFile() != null && !cmdline.getViewerFile().isEmpty())
				file = new File(cmdline.getViewerFile());
			new ViewerWindow(file);
		} else {
			resetProfiles();
		}
	}

	public static void resetProfiles() {
		SystemTray tray = SystemTray.getSystemTray();
		for(TrayIcon icon : tray.getTrayIcons()) {
			tray.remove(icon);
		}

		Sniper mainProfile;
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
		String folderToUseString = null;
		try {
			folderToUseString = URLDecoder.decode(SnipSniper.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LogManager.log(ID, "Could not set profiles folder. Error: " + e.getMessage(), Level.SEVERE);
			exit();
		}

		if(folderToUseString != null) {
			File folderToUse = new File(folderToUseString);
			if (folderToUse.getName().endsWith(".jar"))
				jarFolder = folderToUseString.replace(folderToUse.getName(), "");
			else
				jarFolder = folderToUseString;

			mainFolder = jarFolder + "SnipSniper";
			profilesFolder = mainFolder + "/cfg/";
			logFolder = mainFolder + "/logs/";
		}
	}

	public static void exit() {
		LogManager.log(ID, "Exit requested. Goodbye!", Level.INFO);
		System.exit(0);
	}

	public static String getVersion() {
		return VERSION;
	}

	public static String getProfilesFolder() {
		return profilesFolder;
	}

	public static int getProfileCount() {
		return PROFILE_COUNT;
	}

	public static String getLogFolder() {
		return logFolder;
	}

	public static Config getConfig() {
		return config;
	}

	public static boolean isDemo() {
		return isDemo;
	}

	public static void setDemo(boolean state) {
		isDemo = state;
	}

	public static boolean isIdle() {
		return isIdle;
	}

	public static void setIdle(boolean state) {
		isIdle = state;
	}

	public static Sniper getProfile(int id) {
		return profiles[id];
	}

	public static void setProfile(int id, Sniper sniper) {
		profiles[id] = sniper;
	}

	public static boolean hasProfile(int id) {
		return profiles[id] != null;
	}

	public static void removeProfile(int id) {
		profiles[id] = null;
	}

}
