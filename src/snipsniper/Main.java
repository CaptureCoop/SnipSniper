package snipsniper;

import java.io.File;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import snipsniper.systray.Sniper;

public class Main {
	
	public final static String VERSION = "20200911_3";
	
	public static String jarFolder = new File("").getAbsolutePath() + "\\";;
	public static String mainFolder = jarFolder + "SnipSniper";
	public static String profilesFolder = mainFolder + "\\Profiles\\";
	public static String logFolder = mainFolder + "\\logs\\";
	
	public final static int PROFILE_COUNT = 7;
	
	public static Sniper profiles[] = new Sniper[PROFILE_COUNT];
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		System.setProperty("sun.java2d.uiScale", "1.0");
		new File(profilesFolder).mkdirs();
		new File(logFolder).mkdirs();
		Icons.loadResources();
		
		new Sniper(0).cfg.save();
		
		for(int i = 0; i < PROFILE_COUNT; i++) {
			if(new File(profilesFolder + "profile" + (i + 1) + ".txt").exists()) {
				profiles[i] = new Sniper(i + 1);
			}
		}
	}

}
