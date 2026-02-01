package net.snipsniper.utils

import org.capturecoop.defaultdepot.Closable
import org.capturecoop.defaultdepot.files.FileHandle
import org.json.JSONObject
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.URI
import javax.swing.*

class AttributionsWindow(
    parent: JFrame
): JFrame(), Closable {
    var onClose = ArrayList<() -> Unit>()

    init {
        title = "Attributions"
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        minimumSize = Dimension(800, 600)
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                close()
            }
        })

        val attributions = JSONObject(FileHandle.internal("/net/snipsniper/resources/attributions/attributions.json").readText())

        val content = object: JPanel(GridBagLayout()) {
            //Disables scrolling when opening a lengthy license
            override fun scrollRectToVisible(aRect: Rectangle) {}
        }
        val scrollPane = JScrollPane(content)

        val gbc = GridBagConstraints()
        attributions.getJSONArray("thirdparty").map { it as JSONObject }.forEach { attr ->
            val license = attr.getJSONObject("license")
            val licenseText by lazy {
                FileHandle.internal("/net/snipsniper/resources/attributions/${license.getString("file")}").readText()
            }
            val licenseLabel = JTextArea()
            licenseLabel.isVisible = false
            licenseLabel.isEditable = false

            gbc.gridy++
            gbc.fill = GridBagConstraints.HORIZONTAL
            gbc.weightx = 1.0
            content.add(JLabel(attr.getString("name")), gbc)
            gbc.gridy++
            content.add(JPanel(GridBagLayout()).also { ppanel ->
                val _gbc = GridBagConstraints()
                _gbc.gridx = 0
                ppanel.add(JButton("Website").apply { addActionListener {
                    Desktop.getDesktop().browse(URI(attr.getString("url")))
                } }, _gbc)
                _gbc.gridx = 1
                ppanel.add(JButton(license.getString("short")).apply { addActionListener {
                    licenseLabel.text = licenseText
                    licenseLabel.isVisible = !licenseLabel.isVisible
                } }, _gbc)
                _gbc.gridx = 2
                _gbc.fill = GridBagConstraints.HORIZONTAL
                _gbc.weightx = 1.0
                ppanel.add(Box.createHorizontalGlue(), _gbc)
            }, gbc)
            gbc.gridy++
            content.add(licenseLabel, gbc)
            gbc.gridy++
            content.add(JSeparator(), gbc)
        }

        gbc.gridy++
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        content.add(Box.createVerticalGlue(), gbc);

        scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
        scrollPane.verticalScrollBar.unitIncrement = 32
        add(scrollPane)

        centerOn(parent)
        isVisible = true
    }

    override fun close() {
        onClose.forEach { it.invoke() }
        dispose()
    }
}