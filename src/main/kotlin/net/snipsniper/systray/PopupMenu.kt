package net.snipsniper.systray

import net.snipsniper.utils.scaled
import net.snipsniper.utils.toImageIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.JMenu

class PopupMenu(text: String, icon: BufferedImage): JMenu() {
    init {
        setText(text)
        setIcon(icon.scaled(16, 16).toImageIcon())
        addMouseListener(object: MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                super.mouseEntered(e)
                isArmed = true
                isPopupMenuVisible = true
                popupMenu.setLocation(locationOnScreen.x + width, locationOnScreen.y)
            }

            override fun mouseExited(e: MouseEvent?) {
                super.mouseExited(e)
                isArmed = false
            }
        })
    }

    fun add(menuItem: PopupMenuButton) {
        super.add(menuItem)
        menuItem.setIsMenuChild(true)
        menuItem.addActionListener { isPopupMenuVisible = true }
    }
}