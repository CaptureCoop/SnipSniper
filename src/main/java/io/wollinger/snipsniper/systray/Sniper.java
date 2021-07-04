package io.wollinger.snipsniper.systray;

import java.awt.AWTException;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.utils.*;

import org.apache.commons.lang3.SystemUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.capturewindow.CaptureWindow;
import io.wollinger.snipsniper.configwindow.ConfigWindow;
import io.wollinger.snipsniper.systray.buttons.btnAbout;
import io.wollinger.snipsniper.systray.buttons.btnConfig;
import io.wollinger.snipsniper.systray.buttons.btnExit;
import io.wollinger.snipsniper.systray.buttons.btnOpenImgFolder;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

public class Sniper implements NativeKeyListener, NativeMouseListener {
	public int profileID; //0 = default
	
	CaptureWindow cWnd;
	public ConfigWindow cfgWnd;
	public Config cfg;
	public TrayIcon trayIcon;
	
	Sniper instance;
	
	private final Menu createProfilesMenu = new Menu(LangManager.getItem("menu_create_profile"));
	private final Menu removeProfilesMenu = new Menu(LangManager.getItem("menu_remove_profile"));

	public Sniper(int profileID) {
		instance = this;
		this.profileID = profileID;
		cfg = new Config("profile" + profileID + ".cfg", "CFG" + profileID, "profile_defaults.cfg");

		LogManager.log(getID(), "Loading profile " + profileID, Level.INFO);

		if(SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			PopupMenu popup = new PopupMenu();

			popup.add(new btnOpenImgFolder(this));
			popup.add(new btnConfig(this));

			popup.add(createProfilesMenu);

			popup.add(removeProfilesMenu);

			Menu languageMenu = new Menu(LangManager.getItem("menu_languages"));
			for (String language : LangManager.languages) {
				MenuItem mi = new MenuItem(LangManager.getItem("lang_" + language));
				mi.addActionListener(e -> {
					SnipSniper.getConfig().set(ConfigHelper.MAIN.language, language);
					SnipSniper.getConfig().save();
					SnipSniper.resetProfiles();
				});
				languageMenu.add(mi);
			}
			popup.add(languageMenu);

			if (SnipSniper.getConfig().getBool(ConfigHelper.MAIN.debug)) {
				MenuItem consoleItem = new MenuItem("Console");
				consoleItem.addActionListener(e -> SnipSniper.openDebugConsole());
				popup.add(consoleItem);
			}

			popup.add(new btnAbout(this));

			popup.add(new btnExit());

			try {
				trayIcon = new TrayIcon(Icons.icons[profileID], "SnipSniper (Profile " + profileID + ")", popup);
				trayIcon.setImageAutoSize(true);

				trayIcon.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent mouseEvent) {
						if (mouseEvent.getButton() == 1) {
							if (cWnd == null && SnipSniper.isIdle()) {
								cWnd = new CaptureWindow(instance);
								SnipSniper.setIdle(false);
							}
						}
					}

					@Override
					public void mouseEntered(MouseEvent mouseEvent) {
					}

					@Override
					public void mouseExited(MouseEvent mouseEvent) {
					}

					@Override
					public void mousePressed(MouseEvent mouseEvent) {
					}

					@Override
					public void mouseReleased(MouseEvent mouseEvent) {
						if (mouseEvent.getButton() == MouseEvent.BUTTON3)
							refreshProfiles();
					}

				});

				tray.add(trayIcon);
			} catch (AWTException e) {
				LogManager.log(getID(), "There was an issue setting up the Tray Icon! Message: " + e.getMessage(), Level.SEVERE);
				e.printStackTrace();
			}
		}

		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
	}

	//This refreshes the buttons so that they only show profiles that exist/don't exist respectively.
	void refreshProfiles() {
		LogManager.log(getID(), "Refreshing profiles in task tray", Level.INFO);
		createProfilesMenu.removeAll();
		removeProfilesMenu.removeAll();
		
		for(int i = 0; i < SnipSniper.getProfileCount(); i++) {
			int index = i;
			
			if(!SnipSniper.hasProfile(index)) {
				MenuItem mi = new MenuItem(LangManager.getItem("menu_profile") + " " + (i + 1));
				mi.addActionListener(listener -> addProfile(index));
				createProfilesMenu.add(mi);
			} else if(SnipSniper.hasProfile(index)) {
				MenuItem mi = new MenuItem(LangManager.getItem("menu_profile") + " " + (i + 1));
				mi.addActionListener(listener -> removeProfile(index));
				removeProfilesMenu.add(mi);
			}
		}

	}

	public void kill() {
		GlobalScreen.removeNativeKeyListener(this);
		GlobalScreen.removeNativeMouseListener(this);
		SystemTray.getSystemTray().remove(trayIcon);
		cfg.deleteFile();
		trayIcon = null;
	}

	public void addProfile(int id) {
		if(!SnipSniper.hasProfile(id)) {
			LogManager.log(getID(), "Creating profile " + (id + 1), Level.INFO);
			SnipSniper.setProfile(id, new Sniper(id + 1));
			SnipSniper.getProfile(id).cfg.save();
		}
	}

	public void removeProfile(int id) {
		if(SnipSniper.hasProfile(id)) {
			LogManager.log(getID(), "Removing profile " + (id + 1), Level.INFO);
			SnipSniper.getProfile(id).kill();
			SnipSniper.removeProfile(id);
		}
	}

	public void killCaptureWindow() {
		if(cWnd != null) {
			if(SystemTray.isSupported()) trayIcon.setImage(Icons.icons[profileID]);
			SnipSniper.setIdle(true);
			cWnd.screenshot = null;
			cWnd.screenshotTinted = null;
			cWnd.isRunning = false;
			cWnd.dispose();
			cWnd = null;
			System.gc();
		}
	}

	public void checkNativeKey(String identifier, int pressedKey) {
		String hotkey = cfg.getString(ConfigHelper.PROFILE.hotkey);
		if(!hotkey.equals("NONE")) {
			if(hotkey.startsWith(identifier)) {
				int key = Integer.parseInt(hotkey.replace(identifier, ""));
				if(pressedKey == key) {
					if(cWnd == null && SnipSniper.isIdle()) {
						cWnd = new CaptureWindow(instance);
						SnipSniper.setIdle(false);
					}
				}
			}
		}
	}

	public String getID() {
		return "PROFILE " + profileID;
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
		checkNativeKey("KB", nativeKeyEvent.getKeyCode());
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) { }

	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) { }


	@Override
	public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) { }

	@Override
	public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
		checkNativeKey("M", nativeMouseEvent.getButton());
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) { }
}
