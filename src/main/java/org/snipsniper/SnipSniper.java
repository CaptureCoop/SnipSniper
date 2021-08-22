package org.snipsniper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import org.snipsniper.config.Config;
import org.snipsniper.configwindow.ConfigWindow;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.scviewer.SCViewerWindow;
import org.snipsniper.systray.Sniper;
import org.apache.commons.lang3.SystemUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.snipsniper.utils.*;

public final class SnipSniper {
	public static Config BUILDINFO;
	
	private static String jarFolder;
	private static String mainFolder;
	private static String profilesFolder;
	private static String logFolder;

	private final static int PROFILE_COUNT = 8;

	private static final Sniper[] profiles = new Sniper[PROFILE_COUNT];

	private static boolean isIdle = true;

	private static boolean isDemo = false;

	private static Config config;

	private static DebugConsole debugConsole;

	private final static String ID = "MAIN";

	public static void start(String[] args, boolean saveInDocuments, boolean isEditorOnly, boolean isViewerOnly) {
		if(!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_LINUX) {
			System.out.println("SnipSniper is currently only available for Windows and Linux (In development, use with caution). Sorry!");
			System.exit(0);
		}

		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF); //We do this because otherwise JNativeHook constantly logs stuff



		BUILDINFO = new Config("buildinfo.cfg", "BUILDINFO", "buildinfo.cfg");

		CommandLineHelper cmdline = new CommandLineHelper();
		cmdline.handle(args);

		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException e) {
			LogManager.log(ID, "There was an issue setting up NativeHook! Message: " + e.getMessage(), Level.SEVERE);
		}

		LogManager.setEnabled(true);

		if(saveInDocuments)
			SnipSniper.setSaveLocationToDocuments();
		else
			setSaveLocationToJar();

		if(!isDemo) {
			File tempProfileFolder = new File(profilesFolder);
			File tempLogFolder = new File(logFolder);
			if ((!tempProfileFolder.exists() && !tempProfileFolder.mkdirs()) || (!tempLogFolder.exists() && !tempLogFolder.mkdirs())){
				LogManager.log(ID, "Could not create required folders! Exiting...", Level.SEVERE);
				exit(false);
			}
		}

		config = new Config("main.cfg", "CFG MAIN", "main_defaults.cfg");
		String language = cmdline.getLanguage();
		if(language != null && !language.isEmpty())
			config.set(ConfigHelper.MAIN.language, language);

		if(cmdline.isDebug())
			config.set(ConfigHelper.MAIN.debug, "true");

		LogManager.log(ID, "Loading resources", Level.INFO);
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

			JFrame.setDefaultLookAndFeelDecorated( true );
			JDialog.setDefaultLookAndFeelDecorated( true );
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		if(config.getBool(ConfigHelper.MAIN.debug))
			openDebugConsole();

		LangManager.load();

		LogManager.log(ID, "Launching SnipSniper Version " + getVersion(), Level.INFO);
		if(SystemUtils.IS_OS_LINUX) {
			LogManager.log(ID, "=================================================================================", Level.WARNING);
			LogManager.log(ID, "= SnipSniper Linux is still in development and may not work properly or at all. =", Level.WARNING);
			LogManager.log(ID, "=                        !!!!! USE WITH CAUTION !!!!                            =", Level.WARNING);
			LogManager.log(ID, "=================================================================================", Level.WARNING);
		}

		if(config.getBool(ConfigHelper.MAIN.debug)) {
			LogManager.log(ID, "========================================", Level.INFO);
			LogManager.log(ID, "= SnipSniper is running in debug mode! =", Level.INFO);
			LogManager.log(ID, "========================================", Level.INFO);
		}

		LogManager.log(ID, "Launching language <" + SnipSniper.config.getString(ConfigHelper.MAIN.language) + "> using encoding <" + Charset.defaultCharset() + ">!", Level.INFO);

		if(!LangManager.languages.contains(SnipSniper.config.getString(ConfigHelper.MAIN.language))) {
			LogManager.log(ID, "Language <" + SnipSniper.config.getString(ConfigHelper.MAIN.language) + "> not found. Available languages: " + LangManager.languages.toString(), Level.SEVERE);
			exit(false);
		}

		String wantedEncoding = SnipSniper.config.getString(ConfigHelper.MAIN.encoding);
		if(SnipSniper.config.getBool(ConfigHelper.MAIN.enforceEncoding) && !Charset.defaultCharset().toString().equals(wantedEncoding)) {
			if(!Charset.availableCharsets().containsKey(wantedEncoding)) {
				LogManager.log(ID, "Charset \"" + wantedEncoding + "\" missing. Language \"" + SnipSniper.config.getString(ConfigHelper.MAIN.language)+ "\" not available", Level.SEVERE);
				JOptionPane.showMessageDialog(null, Utils.formatArgs(LangManager.getItem(LangManager.languages.get(0), "error_charset_not_available"), wantedEncoding, SnipSniper.config.getString(ConfigHelper.MAIN.language)));
				exit(false);
			}
			LogManager.log(ID, "Charset <" + wantedEncoding + "> needed! Restarting with correct charset...", Level.WARNING);
			try {
				if(cmdline.isRestartedInstance()) {
					JOptionPane.showMessageDialog(null, "Charset change failed. Please try using a different Java Version! (Java 1.8 / 8)");
					exit(false);
				}
				if(!Utils.restartApplication(wantedEncoding, args))
					LogManager.log(ID, "Restart failed. Possibly not running in jar. Starting with charset <" + Charset.defaultCharset() + ">", Level.WARNING);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}

		config.save();

		if(isDemo) {
			LogManager.log(ID, "============================================================", Level.INFO);
			LogManager.log(ID, "= SnipSniper is running in DEMO mode                       =", Level.INFO);
			LogManager.log(ID, "= This means that no files will be created and/or modified =", Level.INFO);
			LogManager.log(ID, "============================================================", Level.INFO);
		}

		if(cmdline.isEditorOnly() || isEditorOnly) {
			Config config = SCEditorWindow.getStandaloneEditorConfig();
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

			new SCEditorWindow("EDITOR", img, -1, -1, "SnipSniper Editor", config, false, path, false, true);
		} else if(cmdline.isViewerOnly() || isViewerOnly) {
			File file = null;
			if(cmdline.getViewerFile() != null && !cmdline.getViewerFile().isEmpty())
				file = new File(cmdline.getViewerFile());

			if(isViewerOnly) {
				if(args.length > 0) {
					file = new File(args[0]);
					if(!file.exists())
						file = null;
				}
			}

			new SCViewerWindow("VIEW", file, null);
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
			int index = i;
			if (new File(profilesFolder + "profile" + (index) + ".cfg").exists()) {
				profiles[i] = new Sniper(index);
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
			exit(false);
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

	public static void exit(boolean exitForRestart) {
		LogManager.log(ID, "Exit requested. Goodbye!", Level.INFO);
		if(config.getBool(ConfigHelper.MAIN.debug)) {
			if (!exitForRestart && Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().edit(LogManager.getLogFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.exit(0);
	}

	public static String getVersion() {
		return BUILDINFO.getString(ConfigHelper.BUILDINFO.version) + "." + BUILDINFO.getString(ConfigHelper.BUILDINFO.build);
	}

	public static String getProfilesFolder() {
		return profilesFolder;
	}

	public static int getProfileCountMax() {
		return PROFILE_COUNT;
	}

	public static int getProfileCount() {
		int amount = 0;
		for(int i = 0; i < profiles.length; i++)
			if(profiles[i] != null)
				amount++;
		return amount;
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

	public static DebugConsole getDebugConsole() {
		return debugConsole;
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
