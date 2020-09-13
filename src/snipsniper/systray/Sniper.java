package snipsniper.systray;

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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import snipsniper.Config;
import snipsniper.DebugType;
import snipsniper.Icons;
import snipsniper.Main;
import snipsniper.capturewindow.CaptureWindow;
import snipsniper.capturewindow.ImageSelection;
import snipsniper.config.ConfigWindow;
import snipsniper.systray.buttons.btnAbout;
import snipsniper.systray.buttons.btnConfig;
import snipsniper.systray.buttons.btnExit;
import snipsniper.systray.buttons.btnOpenImgFolder;

public class Sniper implements NativeKeyListener{
	public int profileID = 0; //0 = default
	
	CaptureWindow cWnd;
	public ConfigWindow cfgWnd;
	public Config cfg;
	public TrayIcon trayIcon;
	
	Sniper instance;
	
	Menu createProfilesMenu;
	Menu removeProfilesMenu;
	
	File logFile = null;
	
	public Sniper(int _profileID) {
		cfg = new Config(this);
		profileID = _profileID;
		instance = this;
		
		debug("Loading profile " + profileID, DebugType.INFO);
		
	    SystemTray tray = SystemTray.getSystemTray();
	    
	    Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF); //We do this because otherwise JNativeHook constantly logs stuff
	    
		PopupMenu popup = new PopupMenu();
	    
		popup.add(new btnOpenImgFolder(this));
		popup.add(new btnConfig(this));
		popup.add(new btnAbout());

		createProfilesMenu = new Menu("Create profile");
		popup.add(createProfilesMenu);
		
		removeProfilesMenu = new Menu("Remove profile");
		popup.add(removeProfilesMenu);
		
		popup.add(new btnExit());
		
		try {
			trayIcon = new TrayIcon(Icons.icons[profileID], "SnipSniper ", popup );
			trayIcon.setImageAutoSize( true );
			
			trayIcon.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent arg0) {
					if (arg0.getButton() == 1)
		            	if(cWnd == null)
		            		cWnd = new CaptureWindow(instance);	
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
			debug("There was an issue setting up the trayicon! Message: " + e.getMessage(), DebugType.ERROR);
			e.printStackTrace();
		}
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException e) {
			debug("There was an issue setting up NativeHook! Message: " + e.getMessage(), DebugType.ERROR);
			e.printStackTrace();
		}
		GlobalScreen.addNativeKeyListener(this);
	}
	
	//This refreshes the buttons so that they only show profiles that exist/dont exist respectively.
	void refreshProfiles() {
		debug("Refreshing profiles in task tray", DebugType.INFO);
		createProfilesMenu.removeAll();
		removeProfilesMenu.removeAll();
		
		for(int i = 0; i < Main.PROFILE_COUNT; i++) {
			int index = i;
			
			if(Main.profiles[index] == null) {
				MenuItem mi = new MenuItem("Profile " + (i + 1));
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if(Main.profiles[index] == null) {
							Main.profiles[index] = new Sniper(index + 1);
							Main.profiles[index].cfg.save();
						}
					}
				});
				createProfilesMenu.add(mi);
			} else if(Main.profiles[index] != null) {
				MenuItem mi = new MenuItem("Profile " + (i + 1));
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if(Main.profiles[index] != null) {
							cfg.deleteFile();
							SystemTray.getSystemTray().remove(Main.profiles[index].trayIcon);
							GlobalScreen.removeNativeKeyListener(instance);
							Main.profiles[index].trayIcon = null;
							Main.profiles[index] = null;
							instance = null;
						}
					}
				});
				removeProfilesMenu.add(mi);
			}
		}

	}

	public void killCaptureWindow() {
		if(cWnd != null) {
			cWnd.screenshot = null;
			cWnd.screenshotTinted = null;
			cWnd.isRunning = false;
			cWnd.dispose();
			cWnd = null;
		}
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if(e.getKeyCode() == cfg.getInt("hotkey")) {
			if(cWnd == null)
				cWnd = new CaptureWindow(instance);
		} else if(e.getKeyCode() == cfg.getInt("killSwitch") && cfg.getBool("debug")) {
			debug("KillSwitch detected. Goodbye!", DebugType.INFO);
			System.exit(0);
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) { }

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) { }

	public void copyToClipboard(BufferedImage _img) {
		ImageSelection imgSel = new ImageSelection(_img);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
		debug("Copied Image to clipboard", DebugType.INFO);
	}
	
	public boolean saveImage(BufferedImage finalImg, String _modifier) {
		File file;
		LocalDateTime now = LocalDateTime.now();  
		String filename = now.toString().replace(".", "_").replace(":", "_");
		filename += _modifier + ".png";
		String savePath = cfg.getString("pictureFolder");
		File path = new File(savePath);
		file = new File(savePath + filename);
		try {
			if(cfg.getBool("savePictures")) {
				if(!path.exists()) path.mkdirs();
				if(file.createNewFile()) {
					ImageIO.write(finalImg, "png", file);
					debug("Saved image on disk. Location: " + file, DebugType.INFO);
					return true;
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not save image to \"" + file.toString()  + "\"!" , "Error", 1);
			debug("Failed Saving. Wanted Location: " + file, DebugType.WARNING);
			debug("Detailed Error: " + e.getMessage(), DebugType.WARNING);
			e.printStackTrace();
			return false;
		}
		return false;
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
		
		if(cfg.getBool("logTextfile")) {
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
	
}
