package net.snipsniper.sceditor

import net.snipsniper.utils.*
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.cccolorutils.chooser.CCColorChooser
import org.capturecoop.ccutils.utils.CCIClosable
import java.awt.*
import java.awt.event.*
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

        kotlin.run {
            gbc.gridwidth = 2
            gbc.gridx = 0
            add(JLabel("Resize", JLabel.CENTER), gbc)
        }

        var locked = true
        kotlin.run {
            gbc.fill = GridBagConstraints.BOTH
            gbc.gridwidth = 2
            JButton().also {
                fun refresh() {
                    it.icon = "icons/keepscale-${if(locked) "on" else "off"}.png".getImage().rotateClockwise90().toImageIcon()
                }
                it.addActionListener {
                    locked = !locked
                    refresh()
                }
                refresh()
                add(it, gbc)
            }
            gbc.fill = GridBagConstraints.NONE
        }

        gbc.gridwidth = 1
        add(JLabel("Width", JLabel.RIGHT), gbc)
        gbc.gridx = 1
        val widthTextField = JTextField(image.width.toString())
        widthTextField.addKeyListener(object: KeyAdapter(){
            override fun keyReleased(event: KeyEvent) {
                when(event.keyCode) {
                    KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER -> { FocusManager.getCurrentManager().focusNextComponent() }
                }
            }
        })
        widthTextField.addFocusListener(object: FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
            }
        })
        add(widthTextField, gbc)
        gbc.gridx = 0
        add(JLabel("Height", JLabel.RIGHT), gbc)
        gbc.gridx = 1
        val heightTextField = JTextField(image.height.toString())
        add(heightTextField, gbc)

        kotlin.run {
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
        }

        kotlin.run {
            gbc.gridwidth = 1
            JButton("/2").also {
                it.addActionListener {
                    widthTextField.text = (widthTextField.text.toInt() / 2).toString()
                    heightTextField.text = (heightTextField.text.toInt() / 2).toString()
                }
                add(it, gbc)
            }
            gbc.gridx = 1
            JButton("*2").also {
                it.addActionListener {
                    widthTextField.text = (widthTextField.text.toInt() * 2).toString()
                    heightTextField.text = (heightTextField.text.toInt() * 2).toString()
                }
                add(it, gbc)
            }
        }

        gbc.gridwidth = 1
        gbc.gridx = 0
        add(JLabel("Method:"), gbc)
        gbc.gridx = 1
        val dropdown = JComboBox<DropdownItem>().also {
            it.addItem(DropdownItem("Resize", "resize"))
            it.addItem(DropdownItem("Keep Size", "keep-size"))
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
                    //Expand options
                    image = ImageUtils.newBufferedImage(widthInput, heightInput, image.type) { g ->
                        when((dropdown.selectedItem as DropdownItem).id) {
                            "resize" -> { g.drawImage(image, 0, 0, widthInput, heightInput, null) }
                            "keep-size" -> { g.drawImage(image, 0, 0, image.width, image.height, this) }
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