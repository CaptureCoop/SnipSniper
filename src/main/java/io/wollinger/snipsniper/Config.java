package io.wollinger.snipsniper;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;

import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.LogManager;
import io.wollinger.snipsniper.utils.Utils;

public class Config {

	private final HashMap <String, String> settings = new HashMap<>();
	private final HashMap <String, String> defaults = new HashMap<>();

	Sniper sniperInstance;
	
	public Config (Sniper sniperInstance) {
		this.sniperInstance = sniperInstance;
		
		String filename = getFilename(sniperInstance.profileID);
		try {
			if(new File(Main.profilesFolder + filename).exists())
				loadFile(Main.profilesFolder + filename, settings, false);
			
			loadFile("/defaults.txt", defaults, true);
		} catch (NumberFormatException | IOException e) {
			LogManager.log(sniperInstance.profileID, "There was an error loading the config. Message: " + e.getMessage(), Level.SEVERE);
			e.printStackTrace();
		}
	}
	
	void loadFile(String filename, HashMap<String, String> map, boolean inJar) throws IOException, NumberFormatException {
			BufferedReader reader = null;
			if(!inJar)
				reader = new BufferedReader(new FileReader(filename));
			else if(inJar)
				reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			
			String line = reader.readLine();
			
			while (line != null) {
				if(line.contains("=")) {
					String[] args = line.split("=");
					map.put(args[0], args[1]);
				}
				line = reader.readLine();
			}
			reader.close();

	}
	
	public String getRawString(String key) {
		String returnVal = null;
		if(settings.containsKey(key))
			returnVal = settings.get(key);
		else if(defaults.containsKey(key))
			returnVal = defaults.get(key);
		else
			LogManager.log(sniperInstance.profileID, "No value found for <" + key + ">.", Level.SEVERE);
		return returnVal;
	}
	
	public String getString(String key) {
		String str = getRawString(key);
		if(str != null) {
			if(str.contains("%username%")) str = str.replace("%username%", System.getProperty("user.name"));
			if(str.contains("%userprofile%")) str = str.replace("%userprofile%", System.getenv("USERPROFILE"));	
		}
		return str;
	}
	
	public int getInt(String key) {
		if(getString(key) != null)
			return Integer.parseInt(getString(key));
		return -1;
	}

	public float getFloat(String key) {
		if(getString(key) != null)
			return Float.parseFloat(getString(key));
		return -1F;
	}
	
	public boolean getBool(String key) {
		if(getString(key) != null)
			return Boolean.parseBoolean(getString(key));
		return false;
	}
	
	public Color getColor(String key) {
		if(getString(key) != null)
			return Utils.hex2rgb(getString(key));
		return null;
	}
	
	public void set(String key, String value) {
		if(!settings.containsKey(key))
			settings.put(key, value);
		else
			settings.replace(key, value);
	}
	
	String getFilename(int profileID) {
		String filename;
		if(profileID == 0)
			filename = "default.txt";
		else
			filename = "profile" + profileID + ".txt";
		return filename;
	}
	
	public void deleteFile() {
		String filename = getFilename(sniperInstance.profileID);
		File file = new File(Main.profilesFolder + "/" + filename);
		if(!file.delete())
			LogManager.log(sniperInstance.profileID, "Could not delete profile config!", Level.WARNING);
	}
	
	private void saveFile(HashMap<String, String> map) {
		String filename = getFilename(sniperInstance.profileID);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(Main.profilesFolder + "/" + filename));
			for (String key : map.keySet()) {
				String value = map.get(key);
				writer.write(key + "=" + value + "\n");
			}
			writer.close();
		} catch (IOException e) {
			LogManager.log(sniperInstance.profileID, "There was an error saving the config! Message: " + e.getMessage(), Level.SEVERE);
			e.printStackTrace();
		}
	}
	
	public void save() {
		if(!Main.isDemo) {
			if (settings.isEmpty())
				saveFile(defaults);
			else
				saveFile(settings);
		}
	}
	
}
