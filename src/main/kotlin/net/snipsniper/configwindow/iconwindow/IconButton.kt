package net.snipsniper.configwindow.iconwindow

import net.snipsniper.SnipSniper
import net.snipsniper.utils.*
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File

class IconButton(id: String, private val location: SSFile.LOCATION) : IDJButton(id) {
    private var onRedX = false
    private val size = 32
    lateinit var onSelect: (IconButton) -> (Unit)
    lateinit var onDelete: (IconButton) -> (Unit)

    init {
        addActionListener {
            if (onRedX && location == SSFile.LOCATION.LOCAL) {
                File(SnipSniper.imgFolder, SSFile(id).path).delete()
                onDelete.invoke(this)
            } else {
                onSelect.invoke(this)
            }
        }
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(event: MouseEvent) {
                if(location != SSFile.LOCATION.LOCAL) return
                onRedX = Rectangle(width - size, 0, size, size).contains(event.point)
            }
        })
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if(location != SSFile.LOCATION.LOCAL) return
        g.drawImage("icons/redx.png".getImage(), width - size, 0, size, size, this)
    }
}