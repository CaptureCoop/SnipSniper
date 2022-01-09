package net.snipsniper.utils;

import net.snipsniper.LangManager;
import net.snipsniper.LogManager;
import net.snipsniper.SnipSniper;
import net.snipsniper.utils.enums.LaunchType;
import net.snipsniper.utils.enums.LogLevel;
import net.snipsniper.utils.enums.PlatformType;
import net.snipsniper.utils.enums.ReleaseType;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Utils {

	public static PlatformType getPlatformType(String string) {
		if(string == null || string.isEmpty())
			return PlatformType.JAR;

		switch(string.toLowerCase()) {
			case "jar": return PlatformType.JAR;
			case "win": return PlatformType.WIN;
			case "win_installed": return PlatformType.WIN_INSTALLED;
			case "steam": return PlatformType.STEAM;
		}

		return PlatformType.UNKNOWN;
	}

	public static String getTextFromWebsite(String url) {
		try {
			return getTextFromWebsite(new URL(url));
		} catch (MalformedURLException malformedURLException) {
			LogManager.log("Issue forming url: " + url, LogLevel.ERROR);
		}
		return null;
	}

	public static String getHashFromAPI(String link) {
		String text = Utils.getTextFromWebsite(link);
		if(text == null) {
			return null;
		}
		return new JSONObject(text).getString("sha");
	}

	public static int showPopup(Component parent, String message, String title, int optionType, int messageType, BufferedImage icon, boolean blockScreenshot) {
		if(blockScreenshot) SnipSniper.setIdle(false);
		int result = JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType, new ImageIcon(icon.getScaledInstance(32, 32, 0)));
		if(blockScreenshot) SnipSniper.setIdle(true);
		return result;
	}

	public static String getShortGitHash(String longHash) {
		if(longHash == null) return null;
		return longHash.substring(0, 7);
	}

	public static String getTextFromWebsite(URL url) {
		StringBuilder result = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
			in.close();

		} catch (Exception e) {
			LogManager.log("Error requesting text from website (" + url.toString() + "): " + e.getMessage(), LogLevel.ERROR);
		}
		return result.toString();
	}

	public static ReleaseType getReleaseType(String string) {
		switch (string.toLowerCase()) {
			case "release":
			case "stable": return ReleaseType.STABLE;
			case "dev": return ReleaseType.DEV;
			case "dirty": return ReleaseType.DIRTY;
		}
		return ReleaseType.UNKNOWN;
	}

	public static LaunchType getLaunchType(String string) {
		if(string == null || string.isEmpty())
			return LaunchType.NORMAL;
		switch (string.toLowerCase()) {
			case "editor": return LaunchType.EDITOR;
			case "viewer": return LaunchType.VIEWER;
		}
		return LaunchType.NORMAL;
	}

	public static Color getContrastColor(Color color) {
		double y = (299f * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
		return y >= 128 ? Color.black : Color.white;
	}

	//https://stackoverflow.com/questions/4159802/how-can-i-restart-a-java-application
	public static boolean restartApplication(String... args) throws URISyntaxException, IOException {
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final File currentJar = new File(SnipSniper.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		if(!currentJar.getName().endsWith(".jar"))
			return false;

		final ArrayList<String> command = new ArrayList<>();
		command.add(javaBin);
		command.add("-jar");
		command.add(currentJar.getPath());
		command.add("-r");
		Collections.addAll(command, args);

		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.start();
		SnipSniper.exit(true);
		return true;
	}

	public static RenderingHints getRenderingHints() {
		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		return hints;
	}

	public static Dimension getScaledDimension(BufferedImage image, Dimension boundary) {
		return Utils.getScaledDimension(new Dimension(image.getWidth(), image.getHeight()), boundary);
	}

	public static Color getDisabledColor() {
		return new Color(128,128,128, 100);
	}

	//https://stackoverflow.com/a/10245583
	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {
		int original_width = imgSize.width;
		int original_height = imgSize.height;
		int bound_width = boundary.width;
		int bound_height = boundary.height;
		int new_width = original_width;
		int new_height = original_height;

		// first check if we need to scale width
		if (original_width > bound_width) {
			//scale width to fit
			new_width = bound_width;
			//scale height to maintain aspect ratio
			new_height = (new_width * original_height) / original_width;
		}

		// then check if we need to scale even with the new height
		if (new_height > bound_height) {
			//scale height to fit instead
			new_height = bound_height;
			//scale width to maintain aspect ratio
			new_width = (new_height * original_width) / original_height;
		}

		return new Dimension(new_width, new_height);
	}

	public static JComboBox<DropdownItem> getLanguageDropdown(String selectedLanguage, IFunction onSelect) {
		DropdownItem[] langItems = new DropdownItem[LangManager.languages.size()];
		Collections.sort(LangManager.languages);
		DropdownItem selectedItem = null;
		int index = 0;
		for(String lang : LangManager.languages) {
			String translated = LangManager.getItem(lang, "lang_" + lang);
			langItems[index] = new DropdownItem(translated, lang, LangManager.getIcon(lang));
			if(lang.equals(selectedLanguage))
				selectedItem = langItems[index];
			index++;
		}
		JComboBox<DropdownItem> languageDropdown = new JComboBox<>(langItems);
		languageDropdown.setRenderer(new DropdownItemRenderer(langItems));
		languageDropdown.setSelectedItem(selectedItem);
		languageDropdown.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				DropdownItem item = (DropdownItem)languageDropdown.getSelectedItem();
				String id = null;
				if(item != null)
					id = item.getID();

				onSelect.run(id);
			}
		});
		return languageDropdown;
	}

	public static String constructFilename(String format, String modifier) {
		String filename = StringUtils.formatTimeArguments(StringUtils.formatDateArguments(format));
		filename = filename.replaceAll("%random%", StringUtils.getRandomString(10, true, true));
		filename += modifier + ".png";
		return filename;
	}

	public static String rgb2hex(Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	public static Color hex2rgb(String colorStr) {
	    return new Color(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf( colorStr.substring(3, 5), 16), Integer.valueOf(colorStr.substring(5, 7), 16));
	}

	public static void executeProcess(boolean waitTillDone, String... args) {
		try {
			Process process = new ProcessBuilder(args).start();
			if(waitTillDone)
				process.waitFor();
		} catch (IOException | InterruptedException e) {
			LogManager.log("Could not execute process! Args: %c", LogLevel.ERROR, Arrays.toString(args));
			LogManager.logStacktrace(e, LogLevel.ERROR);
		}
	}

	public static boolean containsRectangleFully(Rectangle rectangle, Rectangle contains) {
		return (contains.x + contains.width) < (rectangle.x + rectangle.width) && (contains.x) > (rectangle.x) && (contains.y) > (rectangle.y) && (contains.y + contains.height) < (rectangle.y + rectangle.height);
	}

	public static Rectangle fixRectangle(Rectangle rect) {
		Rectangle newRect = new Rectangle();
		newRect.x = Math.min(rect.x, rect.width);
		newRect.y = Math.min(rect.y, rect.height);
		newRect.width = Math.max(rect.x, rect.width);
		newRect.height = Math.max(rect.y, rect.height);
		return newRect;
	}
}
