package net.snipsniper.utils

import java.awt.Image
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JComboBox

class DropdownItem(private val label: String, val id: String) {
    lateinit var icon: Icon
        private set

    constructor(label: String, id: String, icon: Image) : this(label, id) {
        this.icon = ImageIcon(ImageUtils.imageToBufferedImage(icon).getScaledInstance(16, 16, 0))
    }

    fun compare(other: DropdownItem) = compare(other.id)
    fun compare(id: String): Boolean = this.id == id

    override fun toString(): String = label

    companion object {
        fun setSelected(dropdown: JComboBox<DropdownItem>, id: String): Int {
            for(i in 0 until dropdown.itemCount) {
                if(dropdown.getItemAt(i).compare(id)) {
                    dropdown.selectedIndex = i
                    return i
                }
            }
            return 0
        }
    }
}