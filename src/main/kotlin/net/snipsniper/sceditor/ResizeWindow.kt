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

class ResizeWindow(private var image: BufferedImage, parent: JFrame? = null): JFrame(), CCIClosable {
    var onSubmit: ((BufferedImage) -> (Unit))? = null
    var onClose: (() -> (Unit))? = null

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        layout = GridBagLayout()
        iconImage = "icons/resize.png".getImage()
        title = "Resize"
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
        add(JLabel("Resize", JLabel.CENTER), gbc)
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
        gbc.gridwidth = 1
        gbc.gridx = 0
        add(JLabel("Method:"), gbc)
        gbc.gridx = 1
        val dropdown = JComboBox<DropdownItem>().also {
            it.addItem(DropdownItem("Resize", "resize"))
            add(it, gbc)
        }
        gbc.gridx = 0
        gbc.gridwidth = 2
        JButton("Submit").also {
            it.addActionListener {
                val widthInput = widthTextField.text.toIntOrNull()
                val heightInput = heightTextField.text.toIntOrNull()
                if(widthInput == null || heightInput == null) {
                    Utils.showPopup(this, "Bad input! Not a valid number.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, "icons/redx.png".getImage(), true)
                } else {
                    when((dropdown.selectedItem as DropdownItem).id) {
                        "resize" -> {
                            image = ImageUtils.newBufferedImage(widthInput, heightInput, image.type) { g ->
                                g.drawImage(image, 0, 0, widthInput, heightInput, null)
                            }
                        }
                    }
                    onSubmit?.invoke(image)
                    close()
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
        onClose?.invoke()
        dispose()
    }
}