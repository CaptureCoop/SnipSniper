package org.snipsniper.systray;

import java.awt.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.snipsniper.LangManager;
import org.snipsniper.LogManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.scviewer.SCViewerWindow;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import org.snipsniper.config.Config;
import org.snipsniper.capturewindow.CaptureWindow;
import org.snipsniper.configwindow.ConfigWindow;
import org.snipsniper.systray.buttons.btnAbout;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.snipsniper.utils.*;

import javax.swing.*;

public class Sniper implements NativeKeyListener, NativeMouseListener {
	public int profileID; //0 = default
	
	private CaptureWindow captureWindow;
	private ConfigWindow configWindow;
	private final Config config;
	private TrayIcon trayIcon;
	
	private final Sniper instance;
	private JFrame popup;

	private final static int TASKBAR_HEIGHT = 40;

	public Sniper(int profileID) {
		instance = this;
		this.profileID = profileID;
		config = new Config("profile" + profileID + ".cfg", "CFG" + profileID, "profile_defaults.cfg");

		LogManager.log("Loading profile " + profileID, LogLevel.INFO);

		if(SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			popup = new JFrame();
			popup.setUndecorated(true);
			popup.getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
			popup.setLayout(new BoxLayout(popup.getContentPane(),BoxLayout.PAGE_AXIS));
			BufferedImage splash = Icons.getImage("splash.png");
			JLabel title = new JLabel(new ImageIcon(splash.getScaledInstance((int)(splash.getWidth()/3F),(int)(splash.getHeight()/3F), Image.SCALE_DEFAULT)));
			title.setText("Profile " + profileID);
			title.setAlignmentX(JPanel.CENTER_ALIGNMENT);
			title.setVerticalTextPosition(JLabel.BOTTOM);
			title.setHorizontalTextPosition(JLabel.CENTER);
			popup.add(title);
			popup.add(new PopupMenuButton("Viewer", Icons.getImage("icons/viewer.png"), popup, () -> new SCViewerWindow(null, config)));
			popup.add(new PopupMenuButton("Editor", Icons.getImage("icons/editor.png"), popup, () -> new SCEditorWindow(null, -1, -1, "SnipSniper Editor", config, true, null, false, true)));
			popup.add(new JSeparator());
			popup.add(new PopupMenuButton(LangManager.getItem("menu_open_image_folder"), Icons.getImage("icons/folder.png"), popup, () -> {
				try {
					Desktop.getDesktop().open(new File(getConfig().getString(ConfigHelper.PROFILE.pictureFolder)));
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}));
			popup.add(new PopupMenuButton(LangManager.getItem("menu_config"), Icons.getImage("icons/config.png"), popup, () -> openConfigWindow()));

			if (SnipSniper.getConfig().getBool(ConfigHelper.MAIN.debug)) {
				popup.add(new PopupMenuButton("Console", Icons.getImage("icons/console.png"), popup, () -> SnipSniper.openDebugConsole()));
			}

			popup.add(new btnAbout(LangManager.getItem("menu_about"), Icons.getImage("icons/snipsniper.png"), popup, null));
			popup.add(new JSeparator());
			popup.add(new PopupMenuButton(LangManager.getItem("menu_quit"), Icons.getImage("icons/redx.png"), popup, () -> SnipSniper.exit(false)));

			popup.setIconImage(Icons.getImage("icons/snipsniper.png"));
			popup.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent focusEvent) {
					super.focusLost(focusEvent);
					popup.setVisible(false);
				}
			});

			try {
				trayIcon = new TrayIcon(Icons.getImage("systray/icon" + profileID + ".png"), "SnipSniper (Profile " + profileID + ")");
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
							popup.setVisible(true);
							popup.pack();

							//We do this in order to know which monitor the mouse position is on, before actually placing the popup jframe
							JFrame testGC = new JFrame();
							testGC.setUndecorated(true);
							testGC.setLocation(mouseEvent.getX(), mouseEvent.getY());
							testGC.setVisible(true);
							GraphicsConfiguration gc = testGC.getGraphicsConfiguration();
							testGC.dispose();

							Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
							Rectangle screenRect = gc.getBounds();

							if(screenRect.x != 0 || screenRect.y != 0) {
								//This currently only allows non-default screens to work if taskbar is on bottom. Find better way!!
								//TODO: ^^^^^^^^^^^^^^^^^^
								//IDEA: Take half of the screens width to determine if we are left right bottom or top and then calculate position based on that, if possible
								popup.setLocation(mouseEvent.getX(), mouseEvent.getY() - popup.getHeight() - insets.bottom);
								if(!Utils.containsRectangleFully(screenRect, popup.getBounds())) {
									//Fallback
									//TODO: Find prettier way
									popup.setLocation((int)screenRect.getWidth() / 2 - popup.getWidth() / 2, (int)screenRect.getHeight() / 2 - popup.getHeight() / 2);
								}
							} else {
								if (insets.bottom != 0)
									popup.setLocation(mouseEvent.getX(), screenRect.height - popup.getHeight() - insets.bottom);
								else if (insets.top != 0)
									popup.setLocation(mouseEvent.getX(), insets.top);
								else if (insets.left != 0)
									popup.setLocation(insets.left, mouseEvent.getY() - popup.getHeight());
								else if (insets.right != 0)
									popup.setLocation(screenRect.width - popup.getWidth() - insets.right, mouseEvent.getY() - popup.getHeight());
								else
									popup.setLocation(mouseEvent.getX(), screenRect.height - popup.getHeight() - TASKBAR_HEIGHT);
									/* If "Let taskbar scroll down when not in use" is enabled insets is all 0, use 40 for now, should work fine */
							}
							popup.requestFocus();
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
				LogManager.log("There was an issue setting up the Tray Icon! Message: " + e.getMessage(), LogLevel.ERROR, true);
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
			if(SystemTray.isSupported()) trayIcon.setImage(Icons.getImage("systray/icon" + profileID + ".png"));
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
