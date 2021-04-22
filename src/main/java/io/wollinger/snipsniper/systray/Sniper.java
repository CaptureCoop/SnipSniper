package io.wollinger.snipsniper.systray;

import java.awt.AWTException;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import io.wollinger.snipsniper.utils.LangManager;
import io.wollinger.snipsniper.utils.LogManager;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.Main;
import io.wollinger.snipsniper.capturewindow.CaptureWindow;
import io.wollinger.snipsniper.capturewindow.ImageSelection;
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
		
		LogManager.log(profileID, "Loading profile " + profileID, Level.INFO);
		
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
				Main.config.set("language", language);
				Main.config.save();
				try {
					Main.restartApplication();
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
			trayIcon = new TrayIcon(Icons.icons[profileID], "SnipSniper ", popup );
			trayIcon.setImageAutoSize( true );
			
			trayIcon.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent mouseEvent) {
					if (mouseEvent.getButton() == 1)
		            	if(cWnd == null && Main.isIdle) {
							cWnd = new CaptureWindow(instance);
							Main.isIdle = false;
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
			LogManager.log(profileID, "There was an issue setting up the Tray Icon! Message: " + e.getMessage(), Level.SEVERE);
			e.printStackTrace();
		}
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException e) {
			LogManager.log(profileID, "There was an issue setting up NativeHook! Message: " + e.getMessage(), Level.SEVERE);
			e.printStackTrace();
		}
		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
	}
	
	//This refreshes the buttons so that they only show profiles that exist/don't exist respectively.
	void refreshProfiles() {
		LogManager.log(profileID, "Refreshing profiles in task tray", Level.INFO);
		createProfilesMenu.removeAll();
		removeProfilesMenu.removeAll();
		
		for(int i = 0; i < Main.PROFILE_COUNT; i++) {
			int index = i;
			
			if(Main.profiles[index] == null) {
				MenuItem mi = new MenuItem(LangManager.getItem("menu_profile") + " " + (i + 1));
				mi.addActionListener(listener -> {
					addProfile(index);
				});
				createProfilesMenu.add(mi);
			} else if(Main.profiles[index] != null) {
				MenuItem mi = new MenuItem(LangManager.getItem("menu_profile") + " " + (i + 1));
				mi.addActionListener(listener -> {
					removeProfile(index);
				});
				removeProfilesMenu.add(mi);
			}
		}

	}

	public void addProfile(int id) {
		if(Main.profiles[id] == null) {
			LogManager.log(profileID, "Creating profile " + (id + 1), Level.INFO);
			Main.profiles[id] = new Sniper(id + 1);
			Main.profiles[id].cfg.save();
		}
	}

	public void removeProfile(int id) {
		if(Main.profiles[id] != null) {
			LogManager.log(profileID, "Removing profile " + (id + 1), Level.INFO);
			cfg.deleteFile();
			SystemTray.getSystemTray().remove(Main.profiles[id].trayIcon);
			GlobalScreen.removeNativeKeyListener(instance);
			GlobalScreen.removeNativeMouseListener(instance);
			Main.profiles[id].trayIcon = null;
			Main.profiles[id] = null;
			instance = null;
		}
	}

	public void killCaptureWindow() {
		if(cWnd != null) {
			trayIcon.setImage(Icons.icons[profileID]);
			Main.isIdle = true;
			cWnd.screenshot = null;
			cWnd.screenshotTinted = null;
			cWnd.isRunning = false;
			cWnd.dispose();
			cWnd = null;
			System.gc();
		}
	}

	public void copyToClipboard(BufferedImage img) {
		ImageSelection imgSel = new ImageSelection(img);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
		LogManager.log(profileID, "Copied Image to clipboard", Level.INFO);
	}

	public String constructFilename(String modifier) {
		LocalDateTime now = LocalDateTime.now();
		String filename = now.toString().replace(".", "_").replace(":", "_");
		filename += modifier + ".png";
		return filename;
	}

	public String saveImage(BufferedImage finalImg, String modifier) {
		File file;
		String filename = constructFilename(modifier);
		String savePath = cfg.getString("pictureFolder");
		File path = new File(savePath);
		file = new File(savePath + filename);
		try {
			if(cfg.getBool("saveToDisk")) {
				if(!path.exists()) {
					if(!path.mkdirs()) {
						LogManager.log(profileID, "Failed saving, directory missing & could not create it!", Level.WARNING);
						return null;
					}
				}
				if(file.createNewFile()) {
					ImageIO.write(finalImg, "png", file);
					LogManager.log(profileID, "Saved image on disk. Location: " + file, Level.INFO);
					return file.getAbsolutePath();
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not save image to \"" + file.toString()  + "\"!" , "Error", JOptionPane.INFORMATION_MESSAGE);
			LogManager.log(profileID, "Failed Saving. Wanted Location: " + file, Level.WARNING);
			LogManager.log(profileID, "Detailed Error: " + e.getMessage(), Level.WARNING);
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public void checkNativeKey(String identifier, int pressedKey) {
		String hotkey = cfg.getString("hotkey");
		if(!hotkey.equals("NONE")) {
			if(hotkey.startsWith(identifier)) {
				int key = Integer.parseInt(hotkey.replace(identifier, ""));
				if(pressedKey == key) {
					if(cWnd == null && Main.isIdle) {
						cWnd = new CaptureWindow(instance);
						Main.isIdle = false;
					}
				}
			}
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
	public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
		checkNativeKey("M", nativeMouseEvent.getButton());

	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) { }
}
