package net.snipsniper.configwindow.iconwindow

import net.snipsniper.SnipSniper
import net.snipsniper.utils.*
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class IconButton(id: String, private val location: SSFile.LOCATION) : IDJButton(id) {
    private var onRedX = false
    private val size = 32
    lateinit var onSelect: IFunction
    lateinit var onDelete: IFunction

    init {
        addActionListener {
            if (onRedX && location == SSFile.LOCATION.LOCAL) {
                FileUtils.delete(SnipSniper.imgFolder + "/" + SSFile(id).path)
                onDelete.run()
            } else {
                onSelect.run()
            }
        }
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                super.mousePressed(e)
                if(location == SSFile.LOCATION.LOCAL)
                    onRedX = Rectangle(width - size, 0, size, size).contains(e.point)
            }
        })
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if(location == SSFile.LOCATION.LOCAL)
            g.drawImage("icons/redx.png".getImage(), width - size, 0, size, size, this)
    }
}