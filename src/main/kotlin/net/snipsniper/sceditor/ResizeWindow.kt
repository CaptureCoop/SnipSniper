package net.snipsniper.sceditor

import net.snipsniper.utils.GradientJButton
import net.snipsniper.utils.ImageUtils
import net.snipsniper.utils.Utils
import net.snipsniper.utils.getImage
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.cccolorutils.chooser.CCColorChooser
import org.capturecoop.ccutils.utils.CCIClosable
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import javax.swing.*

class ResizeWindow(private val image: BufferedImage, parent: JFrame? = null): JFrame(), CCIClosable {
    var onSubmit: ((BufferedImage) -> (Unit))? = null
    var onClose: (() -> (Unit))? = null

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
        JButton("Submit").also {
            it.addActionListener {
                val widthInput = widthTextField.text.toIntOrNull()
                val heightInput = heightTextField.text.toIntOrNull()
                if(widthInput == null || heightInput == null) {
                    Utils.showPopup(this, "Bad input! Not a valid number.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, "icons/redx.png".getImage(), true)
                } else {
                    //TODO: Ok?
                    onSubmit?.invoke(image)
                    dispose()
                }
            }
            add(it, gbc)
        }
        pack()
        isVisible = true
        if(parent != null) {
            val posX = location.x + parent.width / 2 - width / 2
            val posY = location.y + parent.height / 2 - height / 2
            setLocation(posX, posY)
        }
    }

    override fun close() {
        onClose?.invoke()
        dispose()
    }
}