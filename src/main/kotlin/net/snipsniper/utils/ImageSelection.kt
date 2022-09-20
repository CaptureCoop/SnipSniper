package net.snipsniper.utils

import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage

//TODO: Is this the cause for the issue when copying images with alpha?
class ImageSelection(private val image: BufferedImage): Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)
    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean = DataFlavor.imageFlavor.equals(flavor)
    override fun getTransferData(flavor: DataFlavor?): Any = if (!DataFlavor.imageFlavor.equals(flavor)) throw UnsupportedFlavorException(flavor) else image
}