package net.snipsniper.systray

import net.snipsniper.utils.getImage
import net.snipsniper.utils.scaled
import net.snipsniper.utils.toImageIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JDialog
import javax.swing.JMenuItem

class PopupMenuButton(title: String, icon: BufferedImage, popup: JDialog, private var onClick: (() -> (Unit))?, closeWhenClicked: ArrayList<PopupMenu>?): JMenuItem() {
    private var isMenuChild = false

    constructor(title: String, iconPath: String, popup: JDialog, onClick: (() -> (Unit))?, closeWhenClicked: ArrayList<PopupMenu>?) : this(title, iconPath.getImage(), popup, onClick, closeWhenClicked)

    init {
        text = title
        this.icon = getPopupIcon(icon)
        addActionListener {
            popup.isVisible = true
            closeWhenClicked?.forEach { it.isPopupMenuVisible = false }
            onClick?.invoke()
        }
        addMouseListener(object: MouseAdapter(){
            override fun mouseEntered(e: MouseEvent?) {
                isArmed = true
                if(!isMenuChild) closeWhenClicked?.forEach { it.isPopupMenuVisible = false }
            }
            override fun mouseExited(e: MouseEvent?) = kotlin.run { isArmed = false }
        })
    }

    fun setIsMenuChild(value: Boolean) = kotlin.run { isMenuChild = value }
    private fun getPopupIcon(image: BufferedImage): ImageIcon = image.scaled(16, 16).toImageIcon()
}