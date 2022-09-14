package net.snipsniper

import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.FileUtils
import org.capturecoop.cclogger.CCLogger
import org.json.JSONArray
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.ImageIcon

//TODO: Draw missing texture in code so that we can always return non null images
class ImageManager {
    companion object {
        private val images = HashMap<String, BufferedImage>()
        private val animatedImages = HashMap<String, Image>()
        private lateinit var filenameList: Array<String>
        private val missingImg = getErrorImage()

        fun loadResources() {
            CCLogger.log("Loading images...")
            val list = JSONArray(FileUtils.loadFileFromJar("img.json"))
            val filenameListTemp = ArrayList<String>()
            for(i in 0 until list.length()) {
                val str = list.getString(i)
                val url = SnipSniper::class.java.getResource("/net/snipsniper/resources/img/$str")
                if(url == null) {
                    CCLogger.log("Could not load image $str!")
                    continue
                }
                filenameListTemp.add(str)
                if(!str.endsWith(".gif")) {
                    images[str] = ImageIO.read(url)
                } else {
                    animatedImages[str] = ImageIcon(url).image
                }
            }
            filenameList = filenameListTemp.toTypedArray()
            CCLogger.log("Done!")
        }

        fun getListAsString(): Array<String> = filenameList

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

        private fun getErrorImage(): BufferedImage {
            val image = BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB)
            val g = image.createGraphics()
            var printBlack = true
            val pinkColor = Color(255, 1, 254)
            for(y in 0 until 8) {
                for(x in 0 until 8) {
                    printBlack = !printBlack
                    g.color = if(printBlack) Color.BLACK else pinkColor
                    g.drawLine(x, y, x, y)
                }
                printBlack = !printBlack
            }
            g.dispose()
            return image
        }
    }
}