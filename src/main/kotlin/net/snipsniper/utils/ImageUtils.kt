package net.snipsniper.utils

import net.snipsniper.ImageManager
import net.snipsniper.SnipSniper
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.Utils.Companion.constructFilename
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import org.capturecoop.ccutils.utils.CCStringUtils
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JOptionPane


class ImageUtils {
    companion object {
        fun getDefaultIcon(profileID: Int): Image = ImageManager.getImage("systray/icon$profileID.png")

        fun ensureAlphaLayer(image: BufferedImage): BufferedImage {
            if(image.type == BufferedImage.TYPE_INT_ARGB) return image

            return BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB).also {
                it.graphics.also { g ->
                    g.drawImage(image, 0, 0, null)
                    g.dispose()
                }
            }
        }

        fun copyToClipboard(image: BufferedImage) {
            ImageSelection(image).let {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(it, null)
                CCLogger.log("Copied Image to clipboard")
            }
        }

        @Synchronized fun copyImage(source: BufferedImage): BufferedImage {
            return BufferedImage(source.width, source.height, source.type).also {
                it.graphics.also { g ->
                    g.drawImage(source, 0, 0, source.width, source.height, null)
                    g.dispose()
                }
            }
        }

        fun rotateClockwise90(src: BufferedImage, times: Int): BufferedImage {
            val w = src.width
            val h = src.height
            return BufferedImage(w, h, src.type).also {
                (it.createGraphics() as Graphics2D).also { g2d ->
                    for(i in 0 until times) {
                        g2d.translate((h - w) / 2, (h - w) / 2)
                        g2d.rotate(Math.PI / 2, (h / 2f).toDouble(), (w / 2f).toDouble())
                    }
                    g2d.drawRenderedImage(src, null)
                }
            }
        }

        fun rotateClockwise90(src: BufferedImage): BufferedImage = rotateClockwise90(src, 1)

        fun getImageFromClipboard(): BufferedImage? {
            //TODO: check if we need try catch here
            Toolkit.getDefaultToolkit().systemClipboard.getContents(null).let {
                if(it.isDataFlavorSupported(DataFlavor.imageFlavor))
                    return it.getTransferData(DataFlavor.imageFlavor) as BufferedImage
            }
            return null
        }

        fun getImageFromDisk(path: String): Image {
            var filePath = path
            if(path.endsWith(".gif")) {
                File.createTempFile("snipsniper_tmp", ".gif").let {
                    Files.copy(File(path).toPath(), it.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    filePath = it.absolutePath
                }
            }
            return ImageIcon(filePath).image
        }

        fun imageToBufferedImage(image: Image): BufferedImage {
            if(image is BufferedImage) return image
            return BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB).also {
                it.createGraphics().also { g ->
                    g.drawImage(image, 0, 0, null)
                    g.dispose()
                }
            }
        }

        fun getIconDynamically(icon: String): Image? {
            if(icon == "none") return null
            SSFile(icon).also {
                return when(it.location) {
                    SSFile.LOCATION.JAR -> {
                        when(icon.endsWith(".gif")) {
                            true -> ImageManager.getAnimatedImage(it.path)
                            else -> ImageManager.getImage(it.path)
                        }
                    }
                    SSFile.LOCATION.LOCAL -> getImageFromDisk(SnipSniper.imgFolder + "/" + it.path)
                    null -> null
                }
            }
        }

        fun getIconDynamically(config: Config): Image? = getIconDynamically(config.getString(ConfigHelper.PROFILE.icon))

        //https://stackoverflow.com/a/36938923
        //Converted by IntelliJ
        fun trimImage(image: BufferedImage): BufferedImage? {
            val raster = image.alphaRaster
            val width = raster.width
            val height = raster.height
            var left = 0
            var top = 0
            var right = width - 1
            var bottom = height - 1
            var minRight = width - 1
            var minBottom = height - 1
            top@ while (top < bottom) {
                for (x in 0 until width) {
                    if (raster.getSample(x, top, 0) != 0) {
                        minRight = x
                        minBottom = top
                        break@top
                    }
                }
                top++
            }
            left@ while (left < minRight) {
                for (y in height - 1 downTo top + 1) {
                    if (raster.getSample(left, y, 0) != 0) {
                        minBottom = y
                        break@left
                    }
                }
                left++
            }
            bottom@ while (bottom > minBottom) {
                for (x in width - 1 downTo left) {
                    if (raster.getSample(x, bottom, 0) != 0) {
                        minRight = x
                        break@bottom
                    }
                }
                bottom--
            }
            right@ while (right > minRight) {
                for (y in bottom downTo top) {
                    if (raster.getSample(right, y, 0) != 0) {
                        break@right
                    }
                }
                right--
            }
            return image.getSubimage(left, top, right - left + 1, bottom - top + 1)
        }

        fun getDragPasteImage(icon: BufferedImage, text: String): BufferedImage {
            return BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB).also {
                val w = it.width
                val h = it.height
                it.createGraphics().also { g ->
                    g.color = Color.WHITE
                    g.fillRect(0,0, w, h)
                    g.color = Color.BLACK
                    g.font = Font("Meiryo", Font.BOLD, 20)
                    g.drawString(text, w / 2 - g.fontMetrics.stringWidth(text) / 2, h / 2)
                    g.drawImage(icon, w / 3,h / 10, w / 3, h / 3, null)
                    g.dispose()
                }
            }
        }

        //TODO: Cleanup
        fun saveImage(finalImg: BufferedImage, format: String, modifier: String, config: Config): String? {
            val filename = constructFilename(format, modifier)
            var savePath = config.getString(ConfigHelper.PROFILE.pictureFolder)
            var pathCustom = config.getString(ConfigHelper.PROFILE.saveFolderCustom)
            if (!pathCustom.startsWith("/")) pathCustom = "/$pathCustom"
            savePath += CCStringUtils.formatDateTimeString(pathCustom)

            var savePathModifier = ""

            if (config.getBool(ConfigHelper.PROFILE.dateFolders)) {
                val currentDate = LocalDate.now()
                val dayString = CCStringUtils.getDateWithProperZero(currentDate.dayOfMonth)
                val monthString = CCStringUtils.getDateWithProperZero(currentDate.monthValue)
                savePathModifier = "\\" + config.getString(ConfigHelper.PROFILE.dateFoldersFormat)
                savePathModifier = savePathModifier.replace("%day%".toRegex(), dayString)
                savePathModifier = savePathModifier.replace("%month%".toRegex(), monthString)
                savePathModifier = savePathModifier.replace("%year%".toRegex(), currentDate.year.toString() + "")
            }

            val path = File(savePath + savePathModifier)
            val file = File(path.absolutePath + "//" + filename)
            try {
                if (!path.exists()) {
                    if (!path.mkdirs()) {
                        CCLogger.log("Failed saving, directory missing & could not create it!", CCLogLevel.WARNING)
                        return null
                    }
                }
                if (file.createNewFile()) {
                    ImageIO.write(finalImg, "png", file)
                    CCLogger.log("Saved image on disk. Location: $file")
                    return file.absolutePath
                }
            } catch (exception: IOException) {
                JOptionPane.showMessageDialog(
                    null,
                    CCStringUtils.format("Could not save image to \"%c\"!", file),
                    "Error",
                    JOptionPane.INFORMATION_MESSAGE
                )
                CCLogger.log("Image could not be saved. Wanted Location: $file", CCLogLevel.WARNING)
                CCLogger.logStacktrace(exception, CCLogLevel.WARNING)
                return null
            }
            return null
        }
    }
}