package net.snipsniper.utils

import java.awt.Component
import java.awt.Image
import javax.swing.*

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

class DropdownItemRenderer(items: Array<DropdownItem>): DefaultListCellRenderer() {
    private val images = HashMap<String, Icon>()

    init {
        items.forEach { images[it.id] = it.icon }
    }

    override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
        val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
        val item = value as DropdownItem
        label.icon = item.icon
        return label
    }
}