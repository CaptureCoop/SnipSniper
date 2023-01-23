package net.snipsniper.sceditor

import net.snipsniper.utils.*
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.cccolorutils.chooser.CCColorChooser
import org.capturecoop.ccutils.utils.CCIClosable
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import javax.swing.*

class NewImageWindow(parent: JFrame? = null): JFrame(), CCIClosable  {
    lateinit var image: BufferedImage
    var onSubmit: (() -> (Unit))? = null
    private val cWindows = ArrayList<CCIClosable>()

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        layout = GridBagLayout()
        iconImage = "icons/editor.png".getImage()
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                super.windowClosing(e)
                close()
            }
        })
        val gbc = GridBagConstraints()
        gbc.insets = Insets(0, 5, 0, 5)
        gbc.gridwidth = 2
        gbc.gridx = 0
        add(JLabel("New Image", JLabel.CENTER), gbc)
        gbc.gridwidth = 1
        add(JLabel("Width", JLabel.RIGHT), gbc)
        gbc.gridx = 1
        val widthTextField = JTextField("512")
        add(widthTextField, gbc)
        gbc.gridx = 0
        add(JLabel("Height", JLabel.RIGHT), gbc)
        gbc.gridx = 1
        val heightTextField = JTextField("512")
        add(heightTextField, gbc)
        gbc.gridx = 0
        gbc.gridwidth = 2
        JButton("Use monitor resolution").also {
            it.addActionListener {
                GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.displayMode.also { dm ->
                    widthTextField.text = dm.width.toString()
                    heightTextField.text = dm.height.toString()
                }
            }
            add(it, gbc)
        }
        val color = CCColor(Color.WHITE)
        GradientJButton("Color", color).also {
            it.addActionListener {
                cWindows.add(CCColorChooser(color, "Color", parent = this, useGradient = true))
            }
            add(it, gbc)
        }
        JButton("Submit").also {
            it.addActionListener {
                val widthInput = widthTextField.text.toIntOrNull()
                val heightInput = heightTextField.text.toIntOrNull()
                if(widthInput == null || heightInput == null) {
                    Utils.showPopup(this, "Bad input! Not a valid number.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, "icons/redx.png".getImage(), true)
                } else {
                    image = ImageUtils.newBufferedImage(widthInput, heightInput) { g ->
                        g as Graphics2D
                        g.paint = color.getGradientPaint(widthInput, heightInput)
                        g.fillRect(0, 0, widthInput, heightInput)
                    }
                    onSubmit?.invoke()
                    dispose()
                }
            }
            add(it, gbc)
        }
        pack()
        if(parent != null) {
            val posX = parent.location.x + parent.width / 2 - width / 2
            val posY = parent.location.y + parent.height / 2 - height / 2
            setLocation(posX, posY)
        }
        isVisible = true
    }

    override fun close() {
        cWindows.forEach { it.close() }
        dispose()
    }
}