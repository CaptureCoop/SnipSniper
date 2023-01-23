package net.snipsniper.utils

import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.FocusManager
import javax.swing.JTextField

class BetterTextField(_text: String, private val accepted: AcceptedInput = AcceptedInput.ANY): JTextField() {
    enum class AcceptedInput { ANY, INT, DOUBLE, FLOAT }
    private var last = _text
    var onFocusLost: ((new: String, old: String) -> (Unit))? = null

    init {
        this.text = _text
        addKeyListener(object: KeyAdapter() {
            override fun keyReleased(event: KeyEvent) {
                when(event.keyCode) {
                    KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER -> { FocusManager.getCurrentManager().clearFocusOwner() }
                }
            }
        })
        addFocusListener(object: FocusAdapter() {
            override fun focusLost(event: FocusEvent) {
                when(accepted) {
                    AcceptedInput.INT -> (text.toDoubleOrNull()?.toInt() ?: text.toIntOrNull() ?: last.toInt()).toString()
                    AcceptedInput.DOUBLE -> (text.toDoubleOrNull() ?: last.toDouble()).toString()
                    AcceptedInput.FLOAT -> (text.toFloatOrNull() ?: last.toFloat()).toString()
                    else -> text
                }.also {
                    text = it
                    onFocusLost?.invoke(it, last)
                    last = it
                }
            }
        })
    }
}