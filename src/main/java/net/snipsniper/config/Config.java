package net.snipsniper.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.capturecoop.cclogger.CCLogger;
import net.snipsniper.SnipSniper;
import net.snipsniper.utils.SSColor;
import net.snipsniper.utils.Utils;
import org.capturecoop.ccutils.utils.StringUtils;;
import org.capturecoop.cclogger.LogLevel;
import org.capturecoop.ccutils.utils.MathUtils;

public class Config {
	private final ConfigContainer settings = new ConfigContainer();
	private final ConfigContainer defaults = new ConfigContainer();

	private String filename;

	public static final String EXTENSION = "cfg";
	public static final String DOT_EXTENSION = "." + EXTENSION;

	public Config (Config config) {
		//Copies config
		LogManager.log("Copying config for <" + config.filename + ">", LogLevel.DEBUG);
		loadFromConfig(config);
	}

	public void loadFromConfig(Config config) {
		this.filename = config.filename;

		settings.loadFromContainer(config.settings);
		defaults.loadFromContainer(config.defaults);
	}

	public Config(String filename, String defaultFile) {
		this(filename, defaultFile, false);
	}

	public Config(String filename, String defaultFile, boolean ignoreLocal) {
		this.filename = filename;
		String idPrefix = filename + ": ";
		LogManager.log(idPrefix + "Creating config object for <" + filename + ">.", LogLevel.DEBUG);
		try {
			String defaultPath = "/net/snipsniper/resources/cfg/" + defaultFile;
			if(!ignoreLocal) {
				String filePath = SnipSniper.getConfigFolder() + filename;
				if (new File(filePath).exists()) {
					LogManager.log(idPrefix + "Config file found locally! Using that one.", LogLevel.DEBUG);
					loadFile(filePath, settings, false);
				} else {
					LogManager.log(idPrefix + "Config file not found locally! Using default.", LogLevel.DEBUG);
					loadFile(defaultPath, settings, true);
				}
			} else {
				LogManager.log(idPrefix + "ignoreLocal is true. Ignoring local file.", LogLevel.DEBUG);
			}

			loadFile(defaultPath, defaults, true);
			LogManager.log(idPrefix + "Done!", LogLevel.DEBUG);
		} catch (NumberFormatException | IOException exception) {
			LogManager.log("There was an error loading the config. Message:", LogLevel.ERROR);
			LogManager.logStacktrace(exception, LogLevel.ERROR);
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
		String returnVal = "<NULL> (" + key + ">";
		if(settings.containsKey(key)) {
			returnVal = settings.get(key);
		} else if(defaults.containsKey(key)) {
			returnVal = defaults.get(key);
		} else {
			LogManager.log("No value found for <%c> in Config <%c>.", LogLevel.ERROR, key, SnipSniper.getConfigFolder() + filename);
			LogManager.logStacktrace(LogLevel.ERROR);
		}
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
			if(MathUtils.isDouble(value))
				return (int) Double.parseDouble(value);
			else return Integer.parseInt(getString(key));
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
		File file = new File(SnipSniper.getConfigFolder() + "/" + filename);
		if(!file.delete())
			LogManager.log("Could not delete profile config!", LogLevel.WARNING);
	}
	
	private void saveFile(ConfigContainer container) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(SnipSniper.getConfigFolder() + "/" + filename));
			for(ConfigOption option : container.getList())
				writer.write(option.toString() + "\n");

			writer.close();
		} catch (IOException exception) {
			LogManager.log("There was an error saving the config! Message:", LogLevel.ERROR);
			LogManager.logStacktrace(exception, LogLevel.ERROR);
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

	public boolean equals(Config other) {
		return settings.equals(other.settings);
	}

	public String getFilename() {
		return filename;
	}
	
}
