package net.snipsniper.utils

import java.awt.Component
import java.awt.Container
import java.awt.GridBagConstraints
import java.awt.Insets
import java.awt.LayoutManager
import java.awt.Rectangle
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

class NonScrollJPanel(layout: LayoutManager): JPanel(layout) {
    override fun scrollRectToVisible(aRect: Rectangle) {}
}

fun JFrame.onClose(action: (WindowEvent) -> Unit) {
    addWindowListener(object: WindowAdapter() {
        override fun windowClosing(event: WindowEvent) {
            action(event)
        }
    })
}

fun JButton.onClick(action: (ActionEvent) -> Unit) {
    addActionListener(action)
}

fun <T: Component> Container.addNew(component: T, constraints: Any? = null, setup: (T.() -> Unit)? = null): T {
    setup?.invoke(component)
    add(component, constraints)
    return component
}

fun JFrame.centerOn(window: JFrame) {
    Utils.centerWindowOnWindow(this, window)
}

private val gbcDefaults = GridBagConstraints()
fun gbc(
    gbc: GridBagConstraints = GridBagConstraints(),
    defaults: GridBagConstraints = gbcDefaults,
    gridx: Int = defaults.gridx,
    gridy: Int = defaults.gridy,
    gridwidth: Int = defaults.gridwidth,
    gridheight: Int = defaults.gridheight,
    weightx: Double = defaults.weightx,
    weighty: Double = defaults.weighty,
    anchor: Int = defaults.anchor,
    fill: Int = defaults.fill,
    insets: Insets = Insets(defaults.insets.top, defaults.insets.left, defaults.insets.bottom, defaults.insets.right),
    ipadx: Int = defaults.ipadx,
    ipady: Int = defaults.ipady
) = gbc.apply {
    this.gridx = gridx
    this.gridy = gridy
    this.gridwidth = gridwidth
    this.gridheight = gridheight
    this.weightx = weightx
    this.weighty = weighty
    this.anchor = anchor
    this.fill = fill
    this.insets = insets
    this.ipadx = ipadx
    this.ipady = ipady
}