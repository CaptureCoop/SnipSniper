package org.snipsniper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import org.snipsniper.config.Config;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.configwindow.ConfigWindow;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.scviewer.SCViewerWindow;
import org.snipsniper.systray.Sniper;
import org.apache.commons.lang3.SystemUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.snipsniper.utils.*;

public final class SnipSniper {
	private static Version version;
	private static Config config;

	private static String jarFolder;
	private static String mainFolder;
	private static String configFolder;
	private static String logFolder;
	private static String imgFolder;

	private final static int PROFILE_COUNT = 8;

	private static final Sniper[] profiles = new Sniper[PROFILE_COUNT];

	private static boolean isIdle = true;

	private static boolean isDemo = false;

	private static DebugConsole debugConsole;

	public static void start(String[] args) {
		if(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_LINUX) {
			System.out.println("SnipSniper is currently only available for Windows and Linux (In development, use with caution). Sorry!");
			System.exit(0);
		}

		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF); //We do this because otherwise JNativeHook constantly logs stuff

		LaunchType launchType = Utils.getLaunchType(System.getProperty("launchType"));

		Config buildinfo = new Config("buildinfo.cfg", "buildinfo.cfg");
		ReleaseType releaseType = Utils.getReleaseType(buildinfo.getString(ConfigHelper.BUILDINFO.type));
		PlatformType platformType = Utils.getPlatformType(System.getProperty("platform"));
		String digits = buildinfo.getString(ConfigHelper.BUILDINFO.version);
		String buildDate = buildinfo.getString(ConfigHelper.BUILDINFO.builddate);
		String githash = buildinfo.getString(ConfigHelper.BUILDINFO.githash);

		version = new Version(digits, releaseType, platformType, buildDate, githash);


		CommandLineHelper cmdline = new CommandLineHelper();
		cmdline.handle(args);

		LogManager.setEnabled(true);

		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException nativeHookException) {
			LogManager.log("There was an issue setting up NativeHook! Message: " + nativeHookException.getMessage(), LogLevel.ERROR);
		}

		if(platformType == PlatformType.STEAM || platformType == PlatformType.WIN_INSTALLED)
			SnipSniper.setSaveLocationToDocuments();
		else
			setSaveLocationToJar();

		if(!isDemo) {
			if(!FileUtils.mkdirs(configFolder, logFolder, imgFolder)) {
				LogManager.log("Could not create required folders! Exiting...", LogLevel.ERROR);
				exit(false);
			}
		}

		config = new Config("main.cfg", "main_defaults.cfg");
		String language = cmdline.getLanguage();
		if(language != null && !language.isEmpty())
			config.set(ConfigHelper.MAIN.language, language);

		if(cmdline.isDebug())
			config.set(ConfigHelper.MAIN.debug, "true");

		LogManager.log("Loading resources", LogLevel.INFO);
		Icons.loadResources();

		System.setProperty("sun.java2d.uiScale", "1.0");
		try {
			if(config.getString(ConfigHelper.MAIN.theme).equals("dark"))
				UIManager.setLookAndFeel(new FlatDarculaLaf());
			else if(config.getString(ConfigHelper.MAIN.theme).equals("light"))
				UIManager.setLookAndFeel(new FlatIntelliJLaf());
			UIManager.put("ScrollBar.showButtons", true);
			UIManager.put("ScrollBar.width", 16 );
			UIManager.put("TabbedPane.showTabSeparators", true);

			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
		} catch (UnsupportedLookAndFeelException e) {
			LogManager.log("Error setting look and feel. Message: " + e.getMessage(), LogLevel.ERROR, true);
		}

		if(config.getBool(ConfigHelper.MAIN.debug))
			openDebugConsole();

		LangManager.load();

		LogManager.log("Launching SnipSniper Version " + getVersion().getDigits() + " (rev-" + version.getGithash() + ")", LogLevel.INFO);
		if(SystemUtils.IS_OS_LINUX) {
			LogManager.log("=================================================================================", LogLevel.WARNING);
			LogManager.log("= SnipSniper Linux is still in development and may not work properly or at all. =", LogLevel.WARNING);
			LogManager.log("=                        !!!!! USE WITH CAUTION !!!!                            =", LogLevel.WARNING);
			LogManager.log("=================================================================================", LogLevel.WARNING);
		}

		if(config.getBool(ConfigHelper.MAIN.debug)) {
			LogManager.log("========================================", LogLevel.INFO);
			LogManager.log("= SnipSniper is running in debug mode! =", LogLevel.INFO);
			LogManager.log("========================================", LogLevel.INFO);
		}

		if(!LangManager.languages.contains(SnipSniper.config.getString(ConfigHelper.MAIN.language))) {
			LogManager.log("Language <" + SnipSniper.config.getString(ConfigHelper.MAIN.language) + "> not found. Available languages: " + LangManager.languages.toString(), LogLevel.ERROR);
			exit(false);
		}

		config.save();

		if(isDemo) {
			LogManager.log("============================================================", LogLevel.INFO);
			LogManager.log("= SnipSniper is running in DEMO mode                       =", LogLevel.INFO);
			LogManager.log("= This means that no files will be created and/or modified =", LogLevel.INFO);
			LogManager.log("============================================================", LogLevel.INFO);
		}

		if(cmdline.isEditorOnly() || launchType == LaunchType.EDITOR) {
			Config config = SCEditorWindow.getStandaloneEditorConfig();
			config.save();

			boolean fileExists;
			BufferedImage img = null;
			String path = "";
			if((cmdline.getEditorFile() != null && !cmdline.getEditorFile().isEmpty()) || (launchType == LaunchType.EDITOR && args.length > 0)) {
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

			new SCEditorWindow(img, -1, -1, "SnipSniper Editor", config, false, path, false, true);
		} else if(cmdline.isViewerOnly() || launchType == LaunchType.VIEWER) {
			File file = null;
			if(cmdline.getViewerFile() != null && !cmdline.getViewerFile().isEmpty())
				file = new File(cmdline.getViewerFile());

			if(launchType == LaunchType.VIEWER) {
				if(args.length > 0) {
					file = new File(args[0]);
					if(!file.exists())
						file = null;
				}
			}

			new SCViewerWindow(file, null, true);
		} else {
			resetProfiles();
		}
	}

	public static void resetProfiles() {
		if(SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			for (TrayIcon icon : tray.getTrayIcons()) {
				tray.remove(icon);
			}
		}

		for(Sniper sniper : profiles) {
			if(sniper != null)
				sniper.kill();
		}

		Arrays.fill(profiles, null);

		profiles[0] = new Sniper(0);
		profiles[0].getConfig().save();

		if(!SystemTray.isSupported()) new ConfigWindow(profiles[0].getConfig(), ConfigWindow.PAGE.snipPanel);
		for (int i = 1; i < PROFILE_COUNT; i++) {
			if (new File(configFolder + "profile" + (i) + ".cfg").exists()) {
				profiles[i] = new Sniper(i);
			}
		}
	}

	public static void setSaveLocationToDocuments() {
		SnipSniper.jarFolder = System.getProperty("user.home");
		SnipSniper.mainFolder = jarFolder + "/.SnipSniper";
		SnipSniper.configFolder = mainFolder + "/cfg/";
		SnipSniper.logFolder = mainFolder + "/logs/";
		SnipSniper.imgFolder = mainFolder + "/img/";
	}

	public static void setSaveLocationToJar() {
		String folderToUseString = null;

		try {
			folderToUseString = URLDecoder.decode(Paths.get(SnipSniper.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(), "UTF-8");
		} catch (UnsupportedEncodingException | URISyntaxException e) {
			LogManager.log("Could not set profiles folder. Error: " + e.getMessage(), LogLevel.ERROR);
			exit(false);
		}

		if(folderToUseString != null) {
			File folderToUse = new File(folderToUseString);
			if (folderToUse.getName().endsWith(".jar"))
				jarFolder = folderToUseString.replace(folderToUse.getName(), "");
			else
				jarFolder = folderToUseString;

			mainFolder = jarFolder + "/SnipSniper";
			configFolder = mainFolder + "/cfg/";
			logFolder = mainFolder + "/logs/";
			imgFolder = mainFolder + "/img/";
		}
	}

	public static void exit(boolean exitForRestart) {
		LogManager.log("Exit requested. Goodbye!", LogLevel.INFO);
		if(config.getBool(ConfigHelper.MAIN.debug)) {
			if (!exitForRestart && Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().open(LogManager.getLogFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.exit(0);
	}

	public static Version getVersion() {
		return version;
	}

	public static String getConfigFolder() {
		return configFolder;
	}

	public static int getProfileCountMax() {
		return PROFILE_COUNT;
	}

	public static int getProfileCount() {
		int amount = 0;
		for (Sniper profile : profiles)
			if (profile != null)
				amount++;
		return amount;
	}

	public static void refreshGlobalConfigFromDisk() {
		config = new Config("main.cfg", "main_defaults.cfg");
	}

	public static String getLogFolder() {
		return logFolder;
	}

	public static String getImageFolder() {
		return imgFolder;
	}

	public static String getMainFolder() {
		return StringUtils.correctSlashes(mainFolder);
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

	public static DebugConsole getDebugConsole() {
		return debugConsole;
	}

	public static void refreshTheme() {
		try {
			if (config.getString(ConfigHelper.MAIN.theme).equals("dark")) {
				UIManager.setLookAndFeel(new FlatDarculaLaf());
			} else if(config.getString(ConfigHelper.MAIN.theme).equals("light")) {
				UIManager.setLookAndFeel(new FlatIntelliJLaf());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void openDebugConsole() {
		if(debugConsole == null) {
			debugConsole = new DebugConsole();
			debugConsole.update();
			debugConsole.addCustomWindowListener(() -> debugConsole = null);
		} else {
			debugConsole.requestFocus();
		}
	}

	public static void closeDebugConsole() {
		if(debugConsole != null) {
			debugConsole.dispose();
			debugConsole = null;
		}
	}
}
