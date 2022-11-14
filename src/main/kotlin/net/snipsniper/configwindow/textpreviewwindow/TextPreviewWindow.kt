package net.snipsniper.configwindow.textpreviewwindow

import net.snipsniper.utils.IFunction
import net.snipsniper.utils.translate
import org.capturecoop.ccutils.utils.CCIClosable
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class TextPreviewWindow(wndTitle: String, var text: String, private val renderPanel: JPanel, icon: BufferedImage, parent: JFrame, explanation: String): JFrame(), CCIClosable {
    private val input = JTextField(text)
    private val saveButton = JButton("config_label_save".translate())
    private val explanationLabel = JLabel("%hour%, %minute%, %second%, %day%, %month%, %year%, %random%")
    var onSave: IFunction? = null

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        title = wndTitle
        explanationLabel.text = explanation
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent) = close()
        })
        addComponentListener(object: ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                Dimension(rootPane.width, contentPane.height - input.height - saveButton.height - explanationLabel.height).also {
                    renderPanel.preferredSize = it
                    renderPanel.minimumSize = it
                    renderPanel.revalidate()
                }
            }
        })
        setSize(256, 256)
        iconImage = icon
        setupUI()
        isVisible = true
        requestFocus()
        pack()
        fun m(l: Int, p: Int, s: Int) = (l + p / 2) - s / 2
        setLocation(m(parent.location.x, parent.width, width), m(parent.location.y, parent.height, height))
    }

    private fun setupUI() {
        JPanel(GridBagLayout()).also { content ->
            val gbc = GridBagConstraints()
            gbc.fill = GridBagConstraints.BOTH
            gbc.gridy = 0
            content.add(renderPanel, gbc)
            gbc.gridy = 1
            input.document.addDocumentListener(object: DocumentListener {
                override fun insertUpdate(e: DocumentEvent) = update()
                override fun removeUpdate(e: DocumentEvent) = update()
                override fun changedUpdate(e: DocumentEvent) = update()

                fun update() {
                    text = input.text
                    renderPanel.repaint()
                }
            })
            content.add(input, gbc)
            gbc.gridy = 2
            content.add(explanationLabel, gbc)
            gbc.gridy = 3
            gbc.fill = GridBagConstraints.VERTICAL
            saveButton.addActionListener {
                onSave?.run()
                dispose()
            }
            content.add(saveButton, gbc)
            add(content)
        }
    }

    override fun close() {
        onSave?.run()
        dispose()
    }
}