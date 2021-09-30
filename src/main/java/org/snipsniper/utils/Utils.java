package org.snipsniper.utils;

import org.json.JSONObject;
import org.snipsniper.ImageManager;
import org.snipsniper.LangManager;
import org.snipsniper.LogManager;
import org.snipsniper.config.Config;
import org.snipsniper.SnipSniper;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.enums.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

	public static BufferedImage getDragPasteImage(BufferedImage icon, String text) {
		BufferedImage dropImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
		Graphics g = dropImage.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0,0,dropImage.getWidth(), dropImage.getHeight());
		g.setColor(Color.BLACK);
		g.setFont(new Font("Meiryo", Font.BOLD, 20));
		int width = g.getFontMetrics().stringWidth(text);
		g.drawString(text, dropImage.getWidth()/2 - width/2, dropImage.getHeight()/2);
		g.drawImage(icon, dropImage.getWidth()/3,dropImage.getHeight()/10, dropImage.getWidth()/3, dropImage.getHeight()/3, null);
		g.dispose();
		return dropImage;
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

	public static Image getImageFromClipboard() {
		Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			try {
				return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Image getImageFromDisk(String path) {
		String filePath = path;
		if(filePath.endsWith(".gif")) {
			try {
				File newFile = new File(System.getProperty("java.io.tmpdir") + "/snipsniper_temp_" + System.currentTimeMillis() + ".gif");
				Files.copy(new File(filePath).toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				filePath = newFile.getAbsolutePath();
			} catch (IOException ioException) {
				LogManager.log("Issue running getImageFromDisk with gif. Message: " + ioException.getMessage(), LogLevel.ERROR);
			}
		}
		return new ImageIcon(filePath).getImage();
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

	public static Image getDefaultIcon(int profileID) {
		return ImageManager.getImage("systray/icon" + profileID + ".png");
	}

	public static Image getIconDynamically(Config config) {
		return getIconDynamically(config.getString(ConfigHelper.PROFILE.icon));
	}

	public static Image getIconDynamically(String icon) {
		SSFile iconFile = new SSFile(icon);
		Image image = null;
		if(icon.equals("none")) {
			return null;
		} else {
			switch (iconFile.getLocation()) {
				case JAR:
					if(!ImageManager.hasImage(iconFile.getPath())) {
						LogManager.log("Couldnt find jar icon. Path: " + iconFile.getPath(), LogLevel.ERROR);
						return null;
					}
					if(icon.endsWith(".gif")) {
						image = ImageManager.getAnimatedImage(iconFile.getPath());
					} else {
						image = ImageManager.getImage(iconFile.getPath());
					}
					return image;
				case LOCAL:
					String path = SnipSniper.getImageFolder() + "/" + iconFile.getPath();
					if(!FileUtils.exists(path))
						LogManager.log("Couldnt find icon. Path: " + path, LogLevel.ERROR);
					else
						image = Utils.getImageFromDisk(SnipSniper.getImageFolder() + "/" + iconFile.getPath());
					return image;
			}
		}
		return null;
	}

	public static BufferedImage imageToBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		BufferedImage image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return image;
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

	public static String saveImage(BufferedImage finalImg, String modifier, Config config) {
		File file;
		String filename = Utils.constructFilename(modifier);
		String savePath = config.getString(ConfigHelper.PROFILE.pictureFolder);
		String pathCustom = config.getString(ConfigHelper.PROFILE.saveFolderCustom);
		if(!pathCustom.startsWith("/"))
			pathCustom = "/" + pathCustom;
		savePath += StringUtils.formatDateArguments(pathCustom);

		String savePathModifier = "";

		if(config.getBool(ConfigHelper.PROFILE.dateFolders)) {
			LocalDate currentDate = LocalDate.now();

			String dayString = StringUtils.getDateWithProperZero(currentDate.getDayOfMonth());
			String monthString = StringUtils.getDateWithProperZero(currentDate.getMonthValue());

			savePathModifier = "\\" + config.getString(ConfigHelper.PROFILE.dateFoldersFormat);
			savePathModifier = savePathModifier.replaceAll("%day%", dayString);
			savePathModifier = savePathModifier.replaceAll("%month%", monthString);
			savePathModifier = savePathModifier.replaceAll("%year%", currentDate.getYear() + "");
		}

		File path = new File(savePath + savePathModifier);
		file = new File(path.getAbsolutePath() + "//" + filename);
		try {
			if(config.getBool(ConfigHelper.PROFILE.saveToDisk)) {
				if(!path.exists()) {
					if(!path.mkdirs()) {
						LogManager.log("Failed saving, directory missing & could not create it!", LogLevel.WARNING);
						return null;
					}
				}
				if(file.createNewFile()) {
					ImageIO.write(finalImg, "png", file);
					LogManager.log("Saved image on disk. Location: " + file, LogLevel.INFO);
					return file.getAbsolutePath();
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Could not save image to \"" + file + "\"!" , "Error", JOptionPane.INFORMATION_MESSAGE);
			LogManager.log("Failed Saving. Wanted Location: " + file, LogLevel.WARNING);
			LogManager.log("Detailed Error: " + e.getMessage(), LogLevel.WARNING);
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public static void copyToClipboard(BufferedImage img) {
		ImageSelection imgSel = new ImageSelection(img);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
		LogManager.log("Copied Image to clipboard", LogLevel.INFO);
	}

	public static String constructFilename(String modifier) {
		LocalDateTime now = LocalDateTime.now();
		String filename = now.toString().replace(".", "_").replace(":", "_");
		filename += modifier + ".png";
		return filename;
	}
	
	public static BufferedImage resizeImage(BufferedImage original, int width, int height) {
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(original, 0,0,width, height, null);
		g.dispose();
		return newImage;
	}

	public static String rgb2hex(Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	public static Color hex2rgb(String colorStr) {
	    return new Color(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf( colorStr.substring(3, 5), 16), Integer.valueOf(colorStr.substring(5, 7), 16));
	}

	public static synchronized BufferedImage copyImage(BufferedImage source){
	    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = b.getGraphics();
	    g.drawImage(source, 0, 0, source.getWidth(), source.getHeight(), null);
	    g.dispose();
	    return b;
	}

	public static BufferedImage rotateClockwise90(BufferedImage src) {
		int width = src.getWidth();
		int height = src.getHeight();

		BufferedImage dest = new BufferedImage(height, width, src.getType());

		Graphics2D graphics2D = dest.createGraphics();
		graphics2D.translate((height - width) / 2, (height - width) / 2);
		graphics2D.rotate(Math.PI / 2, height / 2f, width / 2f);
		graphics2D.drawRenderedImage(src, null);

		return dest;
	}

	public static void executeProcess(boolean waitTillDone, String... args) {
		try {
			Process process = new ProcessBuilder(args).start();
			if(waitTillDone)
				process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
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
