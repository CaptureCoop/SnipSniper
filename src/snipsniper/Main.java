package snipsniper;

import java.io.File;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import snipsniper.systray.Sniper;

public class Main {
	
	public final static String VERSION = "20200909_3";
	
	public static String jarFolder = new File("").getAbsolutePath() + "\\";;
	public static String mainFolder = jarFolder + "SnipSniper";
	public static String profilesFolder = mainFolder + "\\Profiles\\";
	
	public final static int profileCount = 7;
	
	public static Sniper profiles[] = new Sniper[profileCount];
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		System.setProperty("sun.java2d.uiScale", "1.0");
		new File(profilesFolder).mkdirs();
		Icons.loadResources();
		
		new Sniper(0);
		
		for(int i = 0; i < profileCount; i++) {
			if(new File(profilesFolder + "profile" + (i + 1) + ".txt").exists()) {
				profiles[i] = new Sniper(i + 1);
			}
		}
	}

}
