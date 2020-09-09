package snipsniper.systray;

import java.awt.AWTException;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import snipsniper.Icons;
import snipsniper.Main;
import snipsniper.settings;
import snipsniper.capturewindow.CaptureWindow;
import snipsniper.config.ConfigWindow;
import snipsniper.systray.buttons.btnAbout;
import snipsniper.systray.buttons.btnConfig;
import snipsniper.systray.buttons.btnExit;
import snipsniper.systray.buttons.btnOpenImgFolder;

public class Sniper implements NativeKeyListener{
	public int profileID = 0; //0 = default
	
	CaptureWindow cWnd;
	public ConfigWindow cfgWnd;
	public settings cfg;
	public TrayIcon trayIcon;
	
	Sniper instance;
	
	Menu createProfilesMenu;
	Menu removeProfilesMenu;
	
	public Sniper(int _profileID) {
		profileID = _profileID;
		instance = this;
		cfg = new settings(profileID, this);
		
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
					refreshProfiles();
				}
				
			});
			
			tray.add(trayIcon);
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException e1) {
			e1.printStackTrace();
		}
		GlobalScreen.addNativeKeyListener(this);
		
	}
	
	//This refreshes the buttons so that they only show profiles that exist/dont exist respectively.
	void refreshProfiles() {
		createProfilesMenu.removeAll();
		removeProfilesMenu.removeAll();
		
		for(int i = 0; i < Main.profileCount; i++) {
			int index = i;
			
			if(Main.profiles[index] == null) {
				MenuItem mi = new MenuItem("Profile " + (i + 1));
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if(Main.profiles[index] == null) {
							Main.profiles[index] = new Sniper(index + 1);
							Main.profiles[index].cfg.saveFile();
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
			cWnd.dispose();
			cWnd = null;
		}
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if(e.getKeyCode() == cfg.hotkey) {
			if(cWnd == null)
				cWnd = new CaptureWindow(instance);
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) { }

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) { }

	public boolean saveImage(BufferedImage finalImg, String _modifier) {
		File file;
		LocalDateTime now = LocalDateTime.now();  
		String filename = now.toString().replace(".", "_").replace(":", "_");
		filename += _modifier + ".png";
		File path = new File(cfg.pictureFolder);
		file = new File(cfg.pictureFolder + filename);
		try {
			if(cfg.savePictures) {
				if(!path.exists()) path.mkdirs();
				if(file.createNewFile()) {
					ImageIO.write(finalImg, "png", file);
					return true;
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not save image to \"" + file.toString()  + "\"!" , "Error", 1);
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	
}
