package net.snipsniper.utils

import java.awt.*
import java.awt.event.*
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel

class InfoButton(): JButton() {
    private lateinit var info: String
    private var window: JFrame? = null

    constructor(info: String?) : this() {
        this.info = info ?: "No text provided"
        text = "?"
        addMouseListener(object: MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                if(!isEnabled) return
                super.mouseEntered(e)
                Timer().schedule(object: TimerTask() {
                    override fun run() {
                        if(isShowing) {
                            Rectangle(locationOnScreen.x, locationOnScreen.y, bounds.width, bounds.height).also {
                                if(it.contains(MouseInfo.getPointerInfo().location))
                                    openWindow()
                            }
                        }
                    }
                }, 1000)
            }

            override fun mouseExited(e: MouseEvent?) {
                super.mouseExited(e)
                closeWindow()
            }
        })
    }

    override fun paint(g: Graphics) {
        if(!isEnabled) return
        g.color = Color(0, 0, 0, 0)
        g.drawRect(0, 0, width, height)
        val iconSize = 16
        fun fk(n: Int): Int = n / 2 - iconSize / 2
        g.drawImage("icons/questionmark.png".getImage(iconSize, iconSize), fk(width), fk(height), iconSize, iconSize, this)
    }

    fun closeWindow() {
        window?.dispose()
        window = null
    }

    fun openWindow() {
        if(window != null) {
            window?.requestFocus()
            return
        }

        window = JFrame().also {
            it.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            it.setLocation(locationOnScreen.x + width, locationOnScreen.y)
            it.contentPane.background = Color.WHITE
            it.isUndecorated = true
            JLabel("<html><p style=\"width:256px;\">$info</p></html>").also { label ->
                label.verticalAlignment = JLabel.TOP
                it.add(label)
            }
            it.minimumSize = Dimension(256, 128)
            it.rootPane.border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK)
            it.addKeyListener(object: KeyAdapter() {
                override fun keyReleased(e: KeyEvent) {
                    if(e.keyCode == KeyEvent.VK_ESCAPE)
                        it.dispose()
                }
            })
            it.isVisible = true
            it.pack()
        }
    }

}