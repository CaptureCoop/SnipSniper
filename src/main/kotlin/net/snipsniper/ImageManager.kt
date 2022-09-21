package net.snipsniper

import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.FileUtils
import net.snipsniper.utils.ReleaseType
import org.capturecoop.cclogger.CCLogger
import org.json.JSONArray
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.ImageIcon

class ImageManager {
    companion object {
        private val images = HashMap<String, BufferedImage>()
        private val animatedImages = HashMap<String, Image>()
        lateinit var filenameList: Array<String>
            private set
        private const val missingImgSize = 16
        private val missingImgColor = Color(255, 1, 254)
        private val missingImg = getMissingImg()

        fun loadResources() {
            CCLogger.log("Loading images...")
            val list = JSONArray(FileUtils.loadFileFromJar("img.json"))
            val filenameListTemp = ArrayList<String>()
            for(i in 0 until list.length()) {
                val str = list.getString(i)
                val url = SnipSniper::class.java.getResource("/net/snipsniper/resources/img/$str") ?: CCLogger.log("Could not load image $str!")
                if(url !is URL) continue

                filenameListTemp.add(str)
                when(str.endsWith(".gif")) {
                    true -> animatedImages[str] = ImageIcon(url).image
                    false -> images[str] = ImageIO.read(url)
                }
            }
            filenameList = filenameListTemp.toTypedArray()
            CCLogger.log("Done!")
        }

        fun getImage(path: String): BufferedImage = images[path] ?: missingImg.also { CCLogger.log("Could not find image under path $path") }
        fun getAnimatedImage(path: String): Image = animatedImages[path] ?: missingImg.also { CCLogger.log("Could not find image under path $path") }
        fun hasImage(path: String): Boolean = animatedImages.containsKey(path) || images.containsKey(path)

        fun getCodePreview(): BufferedImage {
            return when(SnipSniper.config.getString(ConfigHelper.MAIN.theme)) {
                "dark" -> images["preview/code_dark.png"] ?: missingImg
                "light" -> images["preview/code_light.png"] ?: missingImg
                else -> missingImg
            }
        }

        //Note: This does not handle custom images, that is done in Sniper.kt
        fun getTrayIcon(profileID: Int, alt: Boolean = false): Image {
            if(alt) return getImage("systray/alt_icon$profileID.png")
            if(SnipSniper.buildInfo.releaseType == ReleaseType.STABLE) return getImage("systray/icon$profileID.png")
            return getImage("systray/white_icon$profileID.png")
        }

        private fun getMissingImg(): BufferedImage {
            val image = BufferedImage(missingImgSize, missingImgSize, BufferedImage.TYPE_INT_RGB)
            val g = image.createGraphics()
            var printBlack = true
            for(y in 0 until missingImgSize) {
                for(x in 0 until missingImgSize) {
                    printBlack = !printBlack
                    g.color = if(printBlack) Color.BLACK else missingImgColor
                    g.drawLine(x, y, x, y)
                }
                printBlack = !printBlack
            }
            g.dispose()
            return image
        }
    }
}