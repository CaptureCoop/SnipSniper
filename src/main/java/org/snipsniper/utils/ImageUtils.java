package org.snipsniper.utils;

import org.snipsniper.ImageManager;
import org.snipsniper.LogManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.config.Config;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.enums.LogLevel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class ImageUtils {
    public static Image getDefaultIcon(int profileID) {
        return ImageManager.getImage("systray/icon" + profileID + ".png");
    }

    public static Image getIconDynamically(Config config) {
        return getIconDynamically(config.getString(ConfigHelper.PROFILE.icon));
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

    //https://stackoverflow.com/a/36938923
    public static BufferedImage trimImage(BufferedImage image) {
        WritableRaster raster = image.getAlphaRaster();
        int width = raster.getWidth();
        int height = raster.getHeight();
        int left = 0;
        int top = 0;
        int right = width - 1;
        int bottom = height - 1;
        int minRight = width - 1;
        int minBottom = height - 1;

        top:
        for (;top < bottom; top++){
            for (int x = 0; x < width; x++){
                if (raster.getSample(x, top, 0) != 0){
                    minRight = x;
                    minBottom = top;
                    break top;
                }
            }
        }

        left:
        for (;left < minRight; left++){
            for (int y = height - 1; y > top; y--){
                if (raster.getSample(left, y, 0) != 0){
                    minBottom = y;
                    break left;
                }
            }
        }

        bottom:
        for (;bottom > minBottom; bottom--){
            for (int x = width - 1; x >= left; x--){
                if (raster.getSample(x, bottom, 0) != 0){
                    minRight = x;
                    break bottom;
                }
            }
        }

        right:
        for (;right > minRight; right--){
            for (int y = bottom; y >= top; y--){
                if (raster.getSample(right, y, 0) != 0){
                    break right;
                }
            }
        }

        return image.getSubimage(left, top, right - left + 1, bottom - top + 1);
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
                        image = ImageUtils.getImageFromDisk(SnipSniper.getImageFolder() + "/" + iconFile.getPath());
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
}
