package org.snipsniper.systray;

import java.awt.*;
import java.awt.event.*;

import org.jnativehook.keyboard.NativeKeyAdapter;
import org.jnativehook.mouse.NativeMouseAdapter;
import org.snipsniper.ImageManager;
import org.snipsniper.LogManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.config.ConfigHelper;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;

import org.snipsniper.config.Config;
import org.snipsniper.capturewindow.CaptureWindow;
import org.snipsniper.configwindow.ConfigWindow;
import org.jnativehook.mouse.NativeMouseEvent;
import org.snipsniper.utils.*;
import org.snipsniper.utils.enums.LogLevel;

public class Sniper {
	public int profileID; //0 = default
	
	private CaptureWindow captureWindow;
	private ConfigWindow configWindow;
	private final Config config;
	private TrayIcon trayIcon;
	
	private final Sniper instance;

	private final NativeKeyAdapter nativeKeyAdapter;
	private final NativeMouseAdapter nativeMouseAdapter;

	public Sniper(int profileID) {
		instance = this;
		this.profileID = profileID;
		config = new Config("profile" + profileID + ".cfg", "profile_defaults.cfg");

		LogManager.log("Loading profile " + profileID, LogLevel.DEBUG);

		if(SystemTray.isSupported()) {
			Popup popup = new Popup(this);
			SystemTray tray = SystemTray.getSystemTray();

			try {
				String icon = config.getString(ConfigHelper.PROFILE.icon);
				Image image = ImageUtils.getIconDynamically(icon);

				if(image == null)
					image = ImageUtils.getDefaultIcon(profileID);
				else
					image = image.getScaledInstance(16, 16, Image.SCALE_SMOOTH);

				image.flush();
				trayIcon = new TrayIcon(image, "SnipSniper (" + getTitle() + ")");
				trayIcon.setImageAutoSize(true);

				trayIcon.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent mouseEvent) {
						showPopup(mouseEvent);
					}

					@Override
					public void mousePressed(MouseEvent mouseEvent) {
						showPopup(mouseEvent);
					}

					private void showPopup(MouseEvent mouseEvent) {
						if (mouseEvent.isPopupTrigger())
							popup.showPopup(mouseEvent.getX(), mouseEvent.getY());
					}

					@Override
					public void mouseClicked(MouseEvent mouseEvent) {
						if (mouseEvent.getButton() == 1) {
							if (captureWindow == null && SnipSniper.isIdle()) {
								captureWindow = new CaptureWindow(instance);
								SnipSniper.setIdle(false);
							}
						}
					}
				});

				tray.add(trayIcon);
			} catch (AWTException awtException) {
				LogManager.log("There was an issue setting up the Tray Icon! Message: ", LogLevel.ERROR);
				LogManager.logStacktrace(awtException, LogLevel.ERROR);
			}
		}

		nativeKeyAdapter = new NativeKeyAdapter(){
			@Override
			public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
				checkNativeKey("KB", nativeKeyEvent.getKeyCode(), nativeKeyEvent.getKeyLocation());
			}
		};
		nativeMouseAdapter = new NativeMouseAdapter(){
			@Override
			public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
				checkNativeKey("M", nativeMouseEvent.getButton(), -1);
			}
		};
		GlobalScreen.addNativeKeyListener(nativeKeyAdapter);
		GlobalScreen.addNativeMouseListener(nativeMouseAdapter);
	}

	public void kill() {
		GlobalScreen.removeNativeKeyListener(nativeKeyAdapter);
		GlobalScreen.removeNativeMouseListener(nativeMouseAdapter);
		SystemTray.getSystemTray().remove(trayIcon);
		trayIcon = null;
	}

	public void killCaptureWindow() {
		if(captureWindow != null) {
			if(SystemTray.isSupported() && getIconString().equals("none")) trayIcon.setImage(ImageManager.getImage("systray/icon" + profileID + ".png"));
			SnipSniper.setIdle(true);
			captureWindow.screenshot = null;
			captureWindow.screenshotTinted = null;
			captureWindow.isRunning = false;
			captureWindow.dispose();
			captureWindow = null;
			System.gc();
		}
	}

	public void checkNativeKey(String identifier, int pressedKey, int pressedLocation) {
		String hotkey = config.getString(ConfigHelper.PROFILE.hotkey);
		if(!hotkey.equals("NONE")) {
			if(hotkey.startsWith(identifier)) {
				int location = -1;
				if(pressedLocation != -1 && hotkey.contains("_")) {
					String[] parts =  hotkey.split("_");
					hotkey = parts[0];
					location = Integer.parseInt(parts[1]);
				}
				int key = Integer.parseInt(hotkey.replace(identifier, ""));
				if(pressedKey == key && (location == -1 || location == pressedLocation)) {
					if(captureWindow == null && SnipSniper.isIdle()) {
						captureWindow = new CaptureWindow(instance);
						SnipSniper.setIdle(false);
					}
				}
			}
		}
	}

	public String getIconString() {
		return config.getString(ConfigHelper.PROFILE.icon);
	}

	public String getTitle() {
		String title = config.getString(ConfigHelper.PROFILE.title);
		if(title.equals("none"))
			title = "Profile " + profileID;
		return title;
	}

	public Config getConfig() {
		return config;
	}

	public TrayIcon getTrayIcon() {
		return trayIcon;
	}

	public void openConfigWindow() {
		if(configWindow == null) {
			configWindow = new ConfigWindow(config, ConfigWindow.PAGE.generalPanel);
			configWindow.addCustomWindowListener(() -> configWindow = null);
		} else {
			configWindow.requestFocus();
		}
	}
}
