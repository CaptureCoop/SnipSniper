package net.snipsniper.systray

import net.snipsniper.utils.IFunction
import java.awt.Image
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JMenuItem

class PopupMenuButton(title: String, icon: BufferedImage, popup: JFrame, private var onClick: IFunction?, closeWhenClicked: ArrayList<PopupMenu>?): JMenuItem() {
    private var isMenuChild = false

    init {
        text = title
        this.icon = getPopupIcon(icon)
        addActionListener {
            popup.isVisible = true
            closeWhenClicked?.forEach { it.isPopupMenuVisible = false }
            onClick?.run()
        }
        addMouseListener(object: MouseAdapter(){
            override fun mouseEntered(e: MouseEvent?) {
                super.mouseEntered(e)
                isArmed = true
                if(!isMenuChild) closeWhenClicked?.forEach { it.isPopupMenuVisible = false }
            }

            override fun mouseExited(e: MouseEvent?) {
                super.mouseExited(e)
                isArmed = false
            }
        })
    }

    fun setIsMenuChild(value: Boolean) { isMenuChild = value }
    private fun getPopupIcon(image: BufferedImage): ImageIcon = ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_DEFAULT))
}