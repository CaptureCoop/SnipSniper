package org.snipsniper.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.snipsniper.SnipSniper;
import org.snipsniper.utils.LogManager;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Utils;

public class Config {
	private final ConfigContainer settings = new ConfigContainer();
	private final ConfigContainer defaults = new ConfigContainer();

	private String id;
	private String filename;

	public static final String EXTENSION = "cfg";
	public static final String DOT_EXTENSION = "." + EXTENSION;

	public Config (Config config) {
		//Copies config
		LogManager.log(config.id, "Copying config for <" + config.filename + ">", Level.INFO);
		loadFromConfig(config);
	}

	public void loadFromConfig(Config config) {
		this.filename = config.filename;
		this.id = config.id;

		settings.loadFromContainer(config.settings);
		defaults.loadFromContainer(config.defaults);
	}

	public Config (String filename, String id, String defaultFile) {
		this.filename = filename;
		this.id = id;
		LogManager.log(id, "Creating config object for <" + filename + ">.", Level.INFO);
		try {
			String filePath = SnipSniper.getProfilesFolder() + filename;
			String defaultPath = "/org/snipsniper/resources/cfg/" + defaultFile;
			if(new File(filePath).exists())
				loadFile(filePath, settings, false);
			else
				loadFile(defaultPath, settings, true);

			loadFile(defaultPath, defaults, true);
		} catch (NumberFormatException | IOException e) {
			LogManager.log(id, "There was an error loading the config. Message: " + e.getMessage(), Level.SEVERE);
			e.printStackTrace();
		}
	}
	
	void loadFile(String filename, ConfigContainer container, boolean inJar) throws IOException, NumberFormatException {
			BufferedReader reader;
			if(!inJar)
				reader = new BufferedReader(new FileReader(filename));
			else
				reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			
			String line = reader.readLine();
			
			while (line != null) {
				if(line.startsWith("#")) {
					container.set(line);
				} else if(line.isEmpty() || line.equals(" ")) {
					container.addNewLine();
				} else if(line.contains("=")) {
					String[] args = line.split("=");
					container.set(args[0], args[1]);
				}
				line = reader.readLine();
			}
			reader.close();

	}

	public String getRawString(Enum key) {
		return getRawString(key.toString());
	}

	public String getRawString(String key) {
		String returnVal = null;
		if(settings.containsKey(key))
			returnVal = settings.get(key);
		else if(defaults.containsKey(key))
			returnVal = defaults.get(key);
		else
			LogManager.log(id, "No value found for <" + key + "> in Config <" + SnipSniper.getProfilesFolder() + filename + ">.", Level.SEVERE);
		return returnVal;
	}

	public String getString(Enum key) {
		return getString(key.toString());
	}

	public String getString(String key) {
		String str = getRawString(key);
		if(str != null) {
			str = Utils.replaceVars(str);
		}
		return str;
	}

	public int getInt(Enum key) {
		return getInt(key.toString());
	}

	public int getInt(String key) {
		if(getString(key) != null) {
			String value = getString(key);
			if(Utils.isDouble(value))
				return (int) Double.parseDouble(value);
			else Integer.parseInt(getString(key));
		}
		return -1;
	}

	public float getFloat(Enum key) {
		return getFloat(key.toString());
	}

	public float getFloat(String key) {
		if(getString(key) != null)
			return Float.parseFloat(getString(key));
		return -1F;
	}

	public boolean getBool(Enum key) {
		return getBool(key.toString());
	}

	public boolean getBool(String key) {
		if(getString(key) != null)
			return Boolean.parseBoolean(getString(key));
		return false;
	}

	public SSColor getColor(Enum key) {
		return getColor(key.toString());
	}

	public SSColor getColor(String key) {
		if(getString(key) != null)
			return SSColor.fromSaveString(getString(key));
		return null;
	}

	public void set(Enum key, int value) {
		set(key.toString(), value + "");
	}

	public void set(Enum key, boolean value) {
		set(key.toString(), value + "");
	}

	public void set(Enum key, String value) {
		set(key.toString(), value);
	}

	public void set(String key, String value) {
		settings.set(key, value);
	}
	
	public void deleteFile() {
		File file = new File(SnipSniper.getProfilesFolder() + "/" + filename);
		if(!file.delete())
			LogManager.log(id, "Could not delete profile config!", Level.WARNING);
	}
	
	private void saveFile(ConfigContainer container) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(SnipSniper.getProfilesFolder() + "/" + filename));
			for(ConfigOption option : container.getList())
				writer.write(option.toString() + "\n");

			writer.close();
		} catch (IOException e) {
			LogManager.log(id, "There was an error saving the config! Message: " + e.getMessage(), Level.SEVERE);
			e.printStackTrace();
		}
	}
	
	public void save() {
		if(!SnipSniper.isDemo()) {
			if (settings.isEmpty())
				saveFile(defaults);
			else
				saveFile(settings);
		}
	}

	public String getFilename() {
		return filename;
	}
	
}
