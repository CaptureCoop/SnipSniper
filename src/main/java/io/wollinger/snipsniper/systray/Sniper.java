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
import io.wollinger.snipsniper.utils.LangManager;
import io.wollinger.snipsniper.utils.LogManager;

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
import io.wollinger.snipsniper.utils.Icons;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

public class Sniper implements NativeKeyListener, NativeMouseListener {
	public int profileID; //0 = default
	
	CaptureWindow cWnd;
	public ConfigWindow cfgWnd;
	public Config cfg;
	public TrayIcon trayIcon;
	
	Sniper instance;
	
	Menu createProfilesMenu;
	Menu removeProfilesMenu;
	Menu languageMenu;

	public Sniper(int profileID) {
		this.profileID = profileID;
		
		cfg = new Config("profile" + profileID + ".cfg", "cfg" + profileID, "profile_defaults.cfg");
		instance = this;
		
		LogManager.log(getID(), "Loading profile " + profileID, Level.INFO);
		
	    SystemTray tray = SystemTray.getSystemTray();
	    
	    Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF); //We do this because otherwise JNativeHook constantly logs stuff
	    
		PopupMenu popup = new PopupMenu();

		popup.add(new btnOpenImgFolder(this));
		popup.add(new btnConfig(this));

		createProfilesMenu = new Menu(LangManager.getItem("menu_create_profile"));
		popup.add(createProfilesMenu);
		
		removeProfilesMenu = new Menu(LangManager.getItem("menu_remove_profile"));
		popup.add(removeProfilesMenu);

		languageMenu = new Menu(LangManager.getItem("menu_languages"));
		for(String language : LangManager.languages) {
			MenuItem mi = new MenuItem(LangManager.getItem("lang_" + language));
			mi.addActionListener(e -> {
				SnipSniper.config.set("language", language);
				SnipSniper.config.save();
				try {
					SnipSniper.resetProfiles();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			});
			languageMenu.add(mi);
		}
		popup.add(languageMenu);

		popup.add(new btnAbout(this));

		popup.add(new btnExit());
		
		try {
			trayIcon = new TrayIcon(Icons.icons[profileID], "SnipSniper (Profile " + profileID + ")", popup );
			trayIcon.setImageAutoSize( true );
			
			trayIcon.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent mouseEvent) {
					if (mouseEvent.getButton() == 1)
		            	if(cWnd == null && SnipSniper.isIdle) {
							cWnd = new CaptureWindow(instance);
							SnipSniper.isIdle = false;
		            	}
				}

				@Override
				public void mouseEntered(MouseEvent mouseEvent) { }

				@Override
				public void mouseExited(MouseEvent mouseEvent) { }

				@Override
				public void mousePressed(MouseEvent mouseEvent) { }

				@Override
				public void mouseReleased(MouseEvent mouseEvent) {
					if(mouseEvent.getButton() == MouseEvent.BUTTON3)
						refreshProfiles();
				}
				
			});
			
			tray.add(trayIcon);
		} catch (AWTException e) {
			LogManager.log(getID(), "There was an issue setting up the Tray Icon! Message: " + e.getMessage(), Level.SEVERE);
			e.printStackTrace();
		}
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException e) {
			LogManager.log(getID(), "There was an issue setting up NativeHook! Message: " + e.getMessage(), Level.SEVERE);
			e.printStackTrace();
		}
		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
	}

	public void kill() {
		GlobalScreen.removeNativeKeyListener(this);
		GlobalScreen.removeNativeMouseListener(this);

		SystemTray.getSystemTray().remove(trayIcon);
	}

	//This refreshes the buttons so that they only show profiles that exist/don't exist respectively.
	void refreshProfiles() {
		LogManager.log(getID(), "Refreshing profiles in task tray", Level.INFO);
		createProfilesMenu.removeAll();
		removeProfilesMenu.removeAll();
		
		for(int i = 0; i < SnipSniper.PROFILE_COUNT; i++) {
			int index = i;
			
			if(SnipSniper.profiles[index] == null) {
				MenuItem mi = new MenuItem(LangManager.getItem("menu_profile") + " " + (i + 1));
				mi.addActionListener(listener -> {
					addProfile(index);
				});
				createProfilesMenu.add(mi);
			} else if(SnipSniper.profiles[index] != null) {
				MenuItem mi = new MenuItem(LangManager.getItem("menu_profile") + " " + (i + 1));
				mi.addActionListener(listener -> {
					removeProfile(index);
				});
				removeProfilesMenu.add(mi);
			}
		}

	}

	public void addProfile(int id) {
		if(SnipSniper.profiles[id] == null) {
			LogManager.log(getID(), "Creating profile " + (id + 1), Level.INFO);
			SnipSniper.profiles[id] = new Sniper(id + 1);
			SnipSniper.profiles[id].cfg.save();
		}
	}

	public void removeProfile(int id) {
		if(SnipSniper.profiles[id] != null) {
			LogManager.log(getID(), "Removing profile " + (id + 1), Level.INFO);
			cfg.deleteFile();
			SystemTray.getSystemTray().remove(SnipSniper.profiles[id].trayIcon);
			GlobalScreen.removeNativeKeyListener(instance);
			GlobalScreen.removeNativeMouseListener(instance);
			SnipSniper.profiles[id].trayIcon = null;
			SnipSniper.profiles[id] = null;
			instance = null;
		}
	}

	public void killCaptureWindow() {
		if(cWnd != null) {
			trayIcon.setImage(Icons.icons[profileID]);
			SnipSniper.isIdle = true;
			cWnd.screenshot = null;
			cWnd.screenshotTinted = null;
			cWnd.isRunning = false;
			cWnd.dispose();
			cWnd = null;
			System.gc();
		}
	}

	public void checkNativeKey(String identifier, int pressedKey) {
		String hotkey = cfg.getString("hotkey");
		if(!hotkey.equals("NONE")) {
			if(hotkey.startsWith(identifier)) {
				int key = Integer.parseInt(hotkey.replace(identifier, ""));
				if(pressedKey == key) {
					if(cWnd == null && SnipSniper.isIdle) {
						cWnd = new CaptureWindow(instance);
						SnipSniper.isIdle = false;
					}
				}
			}
		}
	}

	public String getID() {
		return "PRO" + profileID;
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
	public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
		checkNativeKey("M", nativeMouseEvent.getButton());
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) { }
}
