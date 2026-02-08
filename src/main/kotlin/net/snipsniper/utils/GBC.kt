package net.snipsniper.utils

import java.awt.GridBagConstraints
import java.awt.Insets

private val defaults = GridBagConstraints()

fun gbc(
    gbc: GridBagConstraints = GridBagConstraints(),
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