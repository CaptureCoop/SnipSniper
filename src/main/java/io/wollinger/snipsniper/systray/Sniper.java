package io.wollinger.snipsniper.systray;

import java.awt.AWTException;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import io.wollinger.snipsniper.utils.LangManager;
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
import io.wollinger.snipsniper.utils.DebugType;
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
	
	File logFile = null;
	
	public Sniper(int _profileID) {
		profileID = _profileID;
		
		cfg = new Config(this);
		instance = this;
		
		debug("Loading profile " + profileID, DebugType.INFO);
		
	    SystemTray tray = SystemTray.getSystemTray();
	    
	    Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF); //We do this because otherwise JNativeHook constantly logs stuff
	    
		PopupMenu popup = new PopupMenu();

		popup.add(new btnOpenImgFolder(this));
		popup.add(new btnConfig(this));

		createProfilesMenu = new Menu(LangManager.getItem("menu_create_profile", cfg.getString("language")));
		popup.add(createProfilesMenu);
		
		removeProfilesMenu = new Menu(LangManager.getItem("menu_remove_profile", cfg.getString("language")));
		popup.add(removeProfilesMenu);

		popup.add(new btnAbout(this));

		popup.add(new btnExit(this));
		
		try {
			trayIcon = new TrayIcon(Icons.icons[profileID], "SnipSniper ", popup );
			trayIcon.setImageAutoSize( true );
			
			trayIcon.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent arg0) {
					if (arg0.getButton() == 1)
		            	if(cWnd == null && Main.isIdle) {
							cWnd = new CaptureWindow(instance);
							Main.isIdle = false;
		            	}
				}

				@Override
				public void mouseEntered(MouseEvent arg0) { }

				@Override
				public void mouseExited(MouseEvent arg0) { }

				@Override
				public void mousePressed(MouseEvent arg0) { }

				@Override
				public void mouseReleased(MouseEvent arg0) {
					if(arg0.getButton() == MouseEvent.BUTTON3)
						refreshProfiles();
				}
				
			});
			
			tray.add(trayIcon);
		} catch (AWTException e) {
			debug("There was an issue setting up the Tray Icon! Message: " + e.getMessage(), DebugType.ERROR);
			e.printStackTrace();
		}
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException e) {
			debug("There was an issue setting up NativeHook! Message: " + e.getMessage(), DebugType.ERROR);
			e.printStackTrace();
		}
		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
	}
	
	//This refreshes the buttons so that they only show profiles that exist/don't exist respectively.
	void refreshProfiles() {
		debug("Refreshing profiles in task tray", DebugType.INFO);
		createProfilesMenu.removeAll();
		removeProfilesMenu.removeAll();
		
		for(int i = 0; i < Main.PROFILE_COUNT; i++) {
			int index = i;
			
			if(Main.profiles[index] == null) {
				MenuItem mi = new MenuItem("Profile " + (i + 1));
				mi.addActionListener(listener -> {
					if(Main.profiles[index] == null) {
						Main.profiles[index] = new Sniper(index + 1);
						Main.profiles[index].cfg.save();
					}
				});
				createProfilesMenu.add(mi);
			} else if(Main.profiles[index] != null) {
				MenuItem mi = new MenuItem("Profile " + (i + 1));
				mi.addActionListener(listener -> {
					if(Main.profiles[index] != null) {
						cfg.deleteFile();
						SystemTray.getSystemTray().remove(Main.profiles[index].trayIcon);
						GlobalScreen.removeNativeKeyListener(instance);
						GlobalScreen.removeNativeMouseListener(instance);
						Main.profiles[index].trayIcon = null;
						Main.profiles[index] = null;
						instance = null;
					}
				});
				removeProfilesMenu.add(mi);
			}
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

	public void copyToClipboard(BufferedImage _img) {
		ImageSelection imgSel = new ImageSelection(_img);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
		debug("Copied Image to clipboard", DebugType.INFO);
	}

	public String constructFilename(String _modifier) {
		LocalDateTime now = LocalDateTime.now();
		String filename = now.toString().replace(".", "_").replace(":", "_");
		filename += _modifier + ".png";
		return filename;
	}

	public String saveImage(BufferedImage finalImg, String _modifier) {
		File file;
		String filename = constructFilename(_modifier);
		String savePath = cfg.getString("pictureFolder");
		File path = new File(savePath);
		file = new File(savePath + filename);
		try {
			if(cfg.getBool("saveToDisk")) {
				if(!path.exists()) {
					if(!path.mkdirs()) {
						debug("Failed saving, directory missing & could not create it!", DebugType.WARNING);
						return null;
					}
				}
				if(file.createNewFile()) {
					ImageIO.write(finalImg, "png", file);
					debug("Saved image on disk. Location: " + file, DebugType.INFO);
					return file.getAbsolutePath();
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not save image to \"" + file.toString()  + "\"!" , "Error", JOptionPane.INFORMATION_MESSAGE);
			debug("Failed Saving. Wanted Location: " + file, DebugType.WARNING);
			debug("Detailed Error: " + e.getMessage(), DebugType.WARNING);
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	private void debugPrint(String _p, String _t, String _m) {
		String msg = "%DATE% %TIME%[%PROFILE%] [%TYPE%]: %MESSAGE%";
		msg = msg.replace("%PROFILE%", _p);
		msg = msg.replace("%TYPE%", _t);
		msg = msg.replace("%MESSAGE%", _m);
		final LocalDateTime time = LocalDateTime.now();
		msg = msg.replace("%DATE%", "" + DateTimeFormatter.ISO_DATE.format(time));
		msg = msg.replace("%TIME%", "" + DateTimeFormatter.ISO_TIME.format(time));
		
		System.out.println(msg);		
		
		if(cfg.getBool("logTextFile")) {
			if(logFile == null) {
				LocalDateTime now = LocalDateTime.now();  
				String filename = now.toString().replace(".", "_").replace(":", "_");
				filename += ".txt";
				
				logFile = new File(Main.logFolder + filename);
				try {
					if (logFile.createNewFile()) debug("Created new logfile at: " + logFile.getAbsolutePath(), DebugType.INFO);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			msg += "\n";
			
			try {
				Files.write(Paths.get(logFile.getAbsolutePath()), msg.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void debug(String message, DebugType type) {
		int logLvl = cfg.getInt("logLevel");
		if(!cfg.getBool("debug") || logLvl == 0) return;
		
		switch (type) {
			case INFO:
				if(logLvl >= 3) debugPrint("Profile " + profileID, "INFO", message);
				break;
			case WARNING:
				if(logLvl >= 2) debugPrint("Profile " + profileID, "WARNING", message);
				break;
			case ERROR:
				if(logLvl >= 1) debugPrint("Profile " + profileID, "ERROR", message);
				System.exit(0);
		}
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
	public void nativeKeyPressed(NativeKeyEvent e) {
		checkNativeKey("KB", e.getKeyCode());
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) { }

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) { }


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
