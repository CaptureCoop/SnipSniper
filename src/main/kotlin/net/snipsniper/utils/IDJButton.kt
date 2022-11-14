package net.snipsniper.utils

import javax.swing.JButton

open class IDJButton(var id: String): JButton() {
    constructor(id: String, text: String) : this(id) {
        this.text = text
    }
}