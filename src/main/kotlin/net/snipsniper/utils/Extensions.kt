package net.snipsniper.utils

import net.snipsniper.ImageManager
import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.ImageIcon

//Avoid having advanced logic here and put them into fitting Utils packages instead

fun BufferedImage.scale(width: Int, height: Int, hints: Int): BufferedImage = this.getScaledInstance(width, height, hints).toBufferedImage()
fun BufferedImage.scale(width: Int, height: Int): BufferedImage = this.scale(width, height, Image.SCALE_DEFAULT)
fun BufferedImage.scaleFast(width: Int, height: Int): BufferedImage = this.scale(width, height, Image.SCALE_FAST)
fun BufferedImage.scaleSmooth(width: Int, height: Int): BufferedImage = this.scale(width, height, Image.SCALE_SMOOTH)
fun BufferedImage.toImageIcon(): ImageIcon = ImageIcon(this)

fun Image.scaleTo(width: Int, height: Int, hints: Int): Image = this.getScaledInstance(width, height, hints)
fun Image.scaleTo(width: Int, height: Int): Image = this.scaleTo(width, height, Image.SCALE_DEFAULT)
fun Image.scaleFast(width: Int, height: Int): Image = this.scaleTo(width, height, Image.SCALE_FAST)
fun Image.scaleSmooth(width: Int, height: Int): Image = this.scaleTo(width, height, Image.SCALE_SMOOTH)
fun Image.toImageIcon(): ImageIcon = ImageIcon(this)
fun Image.toBufferedImage(): BufferedImage = if(this is BufferedImage) this else ImageUtils.imageToBufferedImage(this)

fun String.getImage(): BufferedImage = ImageManager.getImage(this)
fun String.getImage(width: Int, height: Int): BufferedImage = this.getImage().scale(width, height)
fun String.getAnimatedImage(): Image = ImageManager.getAnimatedImage(this)