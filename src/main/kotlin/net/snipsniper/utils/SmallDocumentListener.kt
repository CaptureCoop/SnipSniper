package net.snipsniper.utils

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SmallDocumentListener(
    private val onEvent: (DocumentEvent) -> Unit
): DocumentListener {
    override fun insertUpdate(event: DocumentEvent) = onEvent(event)
    override fun removeUpdate(event: DocumentEvent) = onEvent(event)
    override fun changedUpdate(event: DocumentEvent) = onEvent(event)
}