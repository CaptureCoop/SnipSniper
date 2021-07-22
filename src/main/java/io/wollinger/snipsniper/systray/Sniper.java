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

import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.utils.*;

import org.jnativehook.GlobalScreen;
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
	
	private CaptureWindow captureWindow;
	private ConfigWindow configWindow;
	private Config config;
	private TrayIcon trayIcon;
	
	private Sniper instance;

	public Sniper(int profileID) {
		instance = this;
		this.profileID = profileID;
		config = new Config("profile" + profileID + ".cfg", "CFG" + profileID, "profile_defaults.cfg");

		LogManager.log(getID(), "Loading profile " + profileID, Level.INFO);

		if(SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			PopupMenu popup = new PopupMenu();

			popup.add(new btnOpenImgFolder(this));
			popup.addSeparator();
			popup.add(new btnConfig(this));

			if (SnipSniper.getConfig().getBool(ConfigHelper.MAIN.debug)) {
				MenuItem consoleItem = new MenuItem("Console");
				consoleItem.addActionListener(e -> SnipSniper.openDebugConsole());
				popup.add(consoleItem);
			}

			popup.add(new btnAbout());
			popup.addSeparator();
			popup.add(new btnExit());

			try {
				trayIcon = new TrayIcon(Icons.icons[profileID], "SnipSniper (Profile " + profileID + ")", popup);
				trayIcon.setImageAutoSize(true);

				trayIcon.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent mouseEvent) {
						if (mouseEvent.getButton() == 1) {
							if (captureWindow == null && SnipSniper.isIdle()) {
								captureWindow = new CaptureWindow(instance);
								SnipSniper.setIdle(false);
							}
						}
					}

					@Override
					public void mouseEntered(MouseEvent mouseEvent) { }

					@Override
					public void mouseExited(MouseEvent mouseEvent) { }

					@Override
					public void mousePressed(MouseEvent mouseEvent) { }

					@Override
					public void mouseReleased(MouseEvent mouseEvent) { }

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

	public void kill() {
		GlobalScreen.removeNativeKeyListener(this);
		GlobalScreen.removeNativeMouseListener(this);
		SystemTray.getSystemTray().remove(trayIcon);
		trayIcon = null;
	}

	public void killCaptureWindow() {
		if(captureWindow != null) {
			if(SystemTray.isSupported()) trayIcon.setImage(Icons.icons[profileID]);
			SnipSniper.setIdle(true);
			captureWindow.screenshot = null;
			captureWindow.screenshotTinted = null;
			captureWindow.isRunning = false;
			captureWindow.dispose();
			captureWindow = null;
			System.gc();
		}
	}

	public void checkNativeKey(String identifier, int pressedKey) {
		String hotkey = config.getString(ConfigHelper.PROFILE.hotkey);
		if(!hotkey.equals("NONE")) {
			if(hotkey.startsWith(identifier)) {
				int key = Integer.parseInt(hotkey.replace(identifier, ""));
				if(pressedKey == key) {
					if(captureWindow == null && SnipSniper.isIdle()) {
						captureWindow = new CaptureWindow(instance);
						SnipSniper.setIdle(false);
					}
				}
			}
		}
	}

	public String getID() {
		return "PROFILE " + profileID;
	}

	public Config getConfig() {
		return config;
	}

	public TrayIcon getTrayIcon() {
		return trayIcon;
	}

	public void openConfigWindow() {
		if(configWindow == null) {
			configWindow = new ConfigWindow(config, ConfigWindow.PAGE.snipPanel);
			configWindow.addCustomWindowListener(() -> configWindow = null);
		} else {
			configWindow.requestFocus();
		}
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
