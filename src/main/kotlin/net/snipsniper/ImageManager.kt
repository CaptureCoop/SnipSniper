package net.snipsniper

import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.FileUtils
import net.snipsniper.utils.ImageUtils
import org.capturecoop.legiblelogger.LegibleLogger
import org.json.JSONArray
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.ImageIcon

object ImageManager {
    private val images = HashMap<String, BufferedImage>()
    private val animatedImages = HashMap<String, Image>()
    lateinit var filenameList: Array<String>
        private set
    private val missingImgColor = Color(255, 1, 254)
    private val missingImg = getMissingImg()

    fun loadResources() {
        LegibleLogger.info("Loading images...")
        val list = JSONArray(FileUtils.loadFileFromJar("img.json"))
        val filenameListTemp = ArrayList<String>()
        for(i in 0 until list.length()) {
            val str = list.getString(i)
            val url = SnipSniper::class.java.getResource("/net/snipsniper/resources/img/$str") ?: LegibleLogger.warn("Could not load image $str!")
            if(url !is URL) continue

            filenameListTemp.add(str)
            when(str.endsWith(".gif")) {
                true -> animatedImages[str] = ImageIcon(url).image
                false -> images[str] = ImageIO.read(url)
            }
        }
        filenameList = filenameListTemp.toTypedArray()
        LegibleLogger.info("Done!")
    }

    fun getImage(path: String): BufferedImage = images[path] ?: missingImg.also { LegibleLogger.warn("Could not find image under path $path") }
    fun getAnimatedImage(path: String): Image = animatedImages[path] ?: missingImg.also { LegibleLogger.warn("Could not find image under path $path") }
    fun hasImage(path: String): Boolean = animatedImages.containsKey(path) || images.containsKey(path)

    fun getCodePreview() = when(SnipSniper.config.getString(ConfigHelper.MAIN.theme)) {
        "dark" -> getImage("preview/code_dark.png")
        "light" -> getImage("preview/code_light.png")
        else -> missingImg
    }

    private fun getMissingImg(size: Int = 16) = ImageUtils.newBufferedImage(size, size, BufferedImage.TYPE_INT_RGB) {
        var printBlack = true
        for(y in 0 until size) {
            for(x in 0 until size) {
                printBlack = !printBlack
                it.color = if(printBlack) Color.BLACK else missingImgColor
                it.drawLine(x, y, x, y)
            }
            printBlack = !printBlack
        }
    }
}
