package io.wollinger.snipsniper.systray;

import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.util.logging.Level;

import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;
import io.wollinger.snipsniper.scviewer.SCViewerWindow;
import io.wollinger.snipsniper.utils.*;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.capturewindow.CaptureWindow;
import io.wollinger.snipsniper.configwindow.ConfigWindow;
import io.wollinger.snipsniper.systray.buttons.btnAbout;
import io.wollinger.snipsniper.systray.buttons.btnOpenImgFolder;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import javax.swing.*;

public class Sniper implements NativeKeyListener, NativeMouseListener {
	public int profileID; //0 = default
	
	private CaptureWindow captureWindow;
	private ConfigWindow configWindow;
	private Config config;
	private TrayIcon trayIcon;
	
	private Sniper instance;
	private JPopupMenu popup;

	public Sniper(int profileID) {
		instance = this;
		this.profileID = profileID;
		config = new Config("profile" + profileID + ".cfg", "CFG" + profileID, "profile_defaults.cfg");

		LogManager.log(getID(), "Loading profile " + profileID, Level.INFO);

		if(SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			popup = new JPopupMenu();
			popup.setLayout(new BoxLayout(popup,BoxLayout.PAGE_AXIS));
			JLabel title = new JLabel(new ImageIcon(Icons.splash.getScaledInstance((int)(Icons.splash.getWidth()/3F),(int)(Icons.splash.getHeight()/3F), Image.SCALE_DEFAULT)));
			title.setText("Profile " + profileID);
			title.setAlignmentX(JPanel.CENTER_ALIGNMENT);
			title.setVerticalTextPosition(JLabel.BOTTOM);
			title.setHorizontalTextPosition(JLabel.CENTER);
			popup.add(title);
			JMenuItem viewer = new JMenuItem("Viewer");
			viewer.setIcon(getPopupIcon(Icons.icon_viewer));
			viewer.addActionListener(e -> new SCViewerWindow("VIEWERWND", null, config));
			popup.add(viewer);
			JMenuItem editor = new JMenuItem("Editor");
			editor.setIcon(getPopupIcon(Icons.icon_editor));
			editor.addActionListener(e -> new SCEditorWindow("EDITORWND", null, -1, -1, "SnipSniper Editor", config, true, null, false, true));
			popup.add(editor);
			popup.addSeparator();
			JMenuItem btnOpenImage = new btnOpenImgFolder(this);
			btnOpenImage.setIcon(getPopupIcon(Icons.icon_questionmark));
			popup.add(btnOpenImage);
			JMenuItem btnConfig = new JMenuItem(LangManager.getItem("menu_config"));
			btnConfig.addActionListener(listener -> openConfigWindow());
			btnConfig.setIcon(getPopupIcon(Icons.icon_config));
			popup.add(btnConfig);

			if (SnipSniper.getConfig().getBool(ConfigHelper.MAIN.debug)) {
				JMenuItem consoleItem = new JMenuItem("Console");
				consoleItem.addActionListener(e -> SnipSniper.openDebugConsole());
				consoleItem.setIcon(getPopupIcon(Icons.icon_console));
				popup.add(consoleItem);
			}

			JMenuItem about = new btnAbout();
			about.setIcon(getPopupIcon(Icons.icon_taskbar));
			popup.add(about);
			popup.addSeparator();
			JMenuItem btnClose = new JMenuItem("Close");
			btnClose.addActionListener(e -> popup.setVisible(false));
			popup.add(btnClose);
			JMenuItem btnExit = new JMenuItem(LangManager.getItem("menu_quit"));
			btnExit.addActionListener(listener -> SnipSniper.exit(false));
			popup.add(btnExit);

			try {
				trayIcon = new TrayIcon(Icons.icons[profileID], "SnipSniper (Profile " + profileID + ")");
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
						if (mouseEvent.isPopupTrigger()) {
							popup.setLocation(mouseEvent.getX(), mouseEvent.getY());
							popup.setInvoker(popup);
							popup.setVisible(true);
						}
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
			} catch (AWTException e) {
				LogManager.log(getID(), "There was an issue setting up the Tray Icon! Message: " + e.getMessage(), Level.SEVERE);
				e.printStackTrace();
			}
		}

		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
	}

	public ImageIcon getPopupIcon(BufferedImage image) {
		return new ImageIcon(image.getScaledInstance(16,16,Image.SCALE_DEFAULT));
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
