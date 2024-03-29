package net.snipsniper.utils

import net.snipsniper.ImageManager
import net.snipsniper.LangManager
import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.ImageIcon

//Avoid having advanced logic here and put them into fitting Utils packages instead

fun BufferedImage.scaled(width: Int, height: Int, hints: Int): BufferedImage = this.getScaledInstance(width, height, hints).toBufferedImage()
fun BufferedImage.scaled(width: Int, height: Int): BufferedImage = this.scaled(width, height, Image.SCALE_DEFAULT)
fun BufferedImage.scaledFast(width: Int, height: Int): BufferedImage = this.scaled(width, height, Image.SCALE_FAST)
fun BufferedImage.scaledSmooth(width: Int, height: Int): BufferedImage = this.scaled(width, height, Image.SCALE_SMOOTH)
fun BufferedImage.toImageIcon(): ImageIcon = ImageIcon(this)
fun BufferedImage.copyToClipboard() = ImageUtils.copyToClipboard(this)
fun BufferedImage.ensureAlphaLayer(): BufferedImage = ImageUtils.ensureAlphaLayer(this)
fun BufferedImage.rotateClockwise90(): BufferedImage = ImageUtils.rotateClockwise90(this)
fun BufferedImage.rotateClockwise90(times: Int): BufferedImage = ImageUtils.rotateClockwise90(this, times)
fun BufferedImage.trim(): BufferedImage = ImageUtils.trimImage(this)
fun BufferedImage.clone(): BufferedImage = ImageUtils.copyImage(this)
fun BufferedImage.flipHorizontally(): BufferedImage = ImageUtils.flipImageHorizontally(this)
fun BufferedImage.flipVertically(): BufferedImage = ImageUtils.flipImageVertically(this)

fun Image.scaled(width: Int, height: Int, hints: Int): Image = this.getScaledInstance(width, height, hints)
fun Image.scaled(width: Int, height: Int): Image = this.scaled(width, height, Image.SCALE_DEFAULT)
fun Image.scaledFast(width: Int, height: Int): Image = this.scaled(width, height, Image.SCALE_FAST)
fun Image.scaledSmooth(width: Int, height: Int): Image = this.scaled(width, height, Image.SCALE_SMOOTH)
fun Image.toImageIcon(): ImageIcon = ImageIcon(this)
fun Image.toBufferedImage(): BufferedImage = if(this is BufferedImage) this else ImageUtils.imageToBufferedImage(this)

fun String.getImage(): BufferedImage = ImageManager.getImage(this)
fun String.getImage(width: Int, height: Int): BufferedImage = this.getImage().scaled(width, height)
fun String.getAnimatedImage(): Image = ImageManager.getAnimatedImage(this)
fun String.translate(): String = LangManager.getItem(this)

fun Long.prettyPrintBytes(): String = Utils.prettyPrintBytes(this)

fun Int.isEven(): Boolean = this % 2 == 0

fun Boolean.toInt() = if(this) 1 else 0