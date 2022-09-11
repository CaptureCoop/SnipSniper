package net.snipsniper.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.capturecoop.cccolorutils.CCColor;
import org.capturecoop.cclogger.CCLogLevel;
import org.capturecoop.cclogger.CCLogger;
import net.snipsniper.SnipSniper;
import net.snipsniper.utils.Utils;
import org.capturecoop.ccutils.utils.CCMathUtils;

public class Config {
	private final ConfigContainer settings = new ConfigContainer();
	private final ConfigContainer defaults = new ConfigContainer();

	private String filename;

	public static final String EXTENSION = "cfg";
	public static final String DOT_EXTENSION = "." + EXTENSION;

	public Config (Config config) {
		//Copies config
		CCLogger.log("Copying config for <" + config.filename + ">", CCLogLevel.DEBUG);
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
		CCLogger.log(idPrefix + "Creating config object for <" + filename + ">.", CCLogLevel.DEBUG);
		try {
			String defaultPath = "/net/snipsniper/resources/cfg/" + defaultFile;
			if(!ignoreLocal) {
				String filePath = SnipSniper.Companion.getConfigFolder() + filename;
				if (new File(filePath).exists()) {
					CCLogger.log(idPrefix + "Config file found locally! Using that one.", CCLogLevel.DEBUG);
					loadFile(filePath, settings, false);
				} else {
					CCLogger.log(idPrefix + "Config file not found locally! Using default.", CCLogLevel.DEBUG);
					loadFile(defaultPath, settings, true);
				}
			} else {
				CCLogger.log(idPrefix + "ignoreLocal is true. Ignoring local file.", CCLogLevel.DEBUG);
			}

			loadFile(defaultPath, defaults, true);
			CCLogger.log(idPrefix + "Done!", CCLogLevel.DEBUG);
		} catch (NumberFormatException | IOException exception) {
			CCLogger.log("There was an error loading the config. Message:", CCLogLevel.ERROR);
			CCLogger.logStacktrace(exception, CCLogLevel.ERROR);
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
			CCLogger.log("No value found for <%c> in Config <%c>.", CCLogLevel.ERROR, key, SnipSniper.Companion.getConfigFolder() + filename);
			CCLogger.logStacktrace(CCLogLevel.ERROR);
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
			if(CCMathUtils.isDouble(value))
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

	public CCColor getColor(Enum key) {
		return getColor(key.toString());
	}

	public CCColor getColor(String key) {
		if(getString(key) != null)
			return CCColor.fromSaveString(getString(key));
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
		File file = new File(SnipSniper.Companion.getConfigFolder() + "/" + filename);
		if(!file.delete())
			CCLogger.log("Could not delete profile config!", CCLogLevel.WARNING);
	}
	
	private void saveFile(ConfigContainer container) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(SnipSniper.Companion.getConfigFolder() + "/" + filename));
			for(ConfigOption option : container.getList())
				writer.write(option.toString() + "\n");

			writer.close();
		} catch (IOException exception) {
			CCLogger.log("There was an error saving the config! Message:", CCLogLevel.ERROR);
			CCLogger.logStacktrace(exception, CCLogLevel.ERROR);
		}
	}
	
	public void save() {
		if(!SnipSniper.Companion.isDemo()) {
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
