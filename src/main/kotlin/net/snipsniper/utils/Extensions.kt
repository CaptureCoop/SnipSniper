package net.snipsniper.utils

import java.awt.Image
import java.awt.image.BufferedImage

//Avoid having advanced logic here and put them into fitting Utils packages instead
fun BufferedImage.scaleSmooth(width: Int, height: Int): BufferedImage = this.getScaledInstance(width, height, Image.SCALE_SMOOTH).toBufferedImage()
fun Image.toBufferedImage(): BufferedImage = ImageUtils.imageToBufferedImage(this)