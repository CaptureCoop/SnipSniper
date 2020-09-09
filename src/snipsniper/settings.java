package snipsniper;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.jnativehook.keyboard.NativeKeyEvent;

import snipsniper.systray.Sniper;

public class settings {
	
	public int hotkey;
	public String pictureFolder;
	public boolean savePictures;
	public boolean copyToClipboard;
	public int borderSize;
	public Color borderColor;
	public int snipeDelay;
	public boolean openEditor;
	
	Sniper sniperInstance;
	
	public settings(int _profileID, Sniper _sniperInstance) {
		sniperInstance = _sniperInstance;
		String filename = getFilename(_profileID);
		try {
			if(new File(Main.profilesFolder + filename).exists())
				loadFile(filename);
			else
				loadDefaults();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			loadDefaults();
		}
		
	}
	
	void loadDefaults() {
		hotkey = NativeKeyEvent.VC_F9;
		pictureFolder = System.getProperty("user.home") + "\\pictures\\SnipSniper\\";
		savePictures = true;
		copyToClipboard = true;
		borderSize = 0;
		borderColor = Color.BLACK;
		snipeDelay = 0;
		openEditor = false;
	}
	
	String getFilename(int _profileID) {
		String filename = "";
		if(_profileID == 0)
			filename = "default.txt";
		else
			filename = "profile" + _profileID + ".txt";
		return filename;
	}
	
	public void deleteFile() {
		String filename = getFilename(sniperInstance.profileID);
		File file = new File(Main.profilesFolder + "\\" + filename);
		file.delete();
	}
	
	public void saveFile() {
		String filename = getFilename(sniperInstance.profileID);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(Main.profilesFolder + "\\" + filename));
			writer.write(hotkey + "\n");
			writer.write(String.valueOf(savePictures) + "\n");
			writer.write(String.valueOf(copyToClipboard) + "\n");
			writer.write(borderSize + "\n");
			writer.write(Utils.rgb2hex(borderColor) + "\n");
			writer.write(pictureFolder + "\n");
			writer.write(snipeDelay + "\n");
			writer.write(String.valueOf(openEditor) + "");
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	void loadFile(String filename) throws IOException, NumberFormatException {
		if(new File(Main.profilesFolder + filename).exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(Main.profilesFolder + filename));
			String line = reader.readLine();
			int lineNr = 0;
			while (line != null) {
				if(lineNr == 0)
					hotkey = Integer.parseInt(line);
				else if(lineNr == 1)
					savePictures = Boolean.parseBoolean(line);
				else if(lineNr == 2)
					copyToClipboard = Boolean.parseBoolean(line);
				else if(lineNr == 3)
					borderSize = Integer.parseInt(line);
				else if(lineNr == 4)
					borderColor = Utils.hex2rgb(line);
				else if(lineNr == 5)
					pictureFolder = line;
				else if(lineNr == 6)
					snipeDelay = Integer.parseInt(line);
				else if(lineNr == 7)
					openEditor = Boolean.parseBoolean(line);
				lineNr++;
				line = reader.readLine();
			}
			reader.close();
		}
	}
}
