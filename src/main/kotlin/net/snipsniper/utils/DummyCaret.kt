package net.snipsniper.utils

import java.awt.Graphics
import java.awt.Point
import javax.swing.event.ChangeListener
import javax.swing.text.Caret
import javax.swing.text.JTextComponent

class DummyCaret: Caret {
    override fun install(c: JTextComponent?) { }
    override fun deinstall(c: JTextComponent?) { }
    override fun paint(g: Graphics?) { }
    override fun addChangeListener(l: ChangeListener?) { }
    override fun removeChangeListener(l: ChangeListener?) { }
    override fun isVisible(): Boolean = false
    override fun setVisible(v: Boolean) { }
    override fun isSelectionVisible(): Boolean = false
    override fun setSelectionVisible(v: Boolean) { }
    override fun setMagicCaretPosition(p: Point?) { }
    override fun getMagicCaretPosition(): Point? = null
    override fun setBlinkRate(rate: Int) { }
    override fun getBlinkRate(): Int = 0
    override fun getDot(): Int = 0
    override fun getMark(): Int = 0
    override fun setDot(dot: Int) { }
    override fun moveDot(dot: Int) { }
}