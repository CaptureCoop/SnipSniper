package net.snipsniper.sceditor.ezmode

import net.snipsniper.SnipSniper.Companion.getNewThread
import net.snipsniper.configwindow.StampJPanel
import net.snipsniper.sceditor.SCEditorWindow
import net.snipsniper.sceditor.stamps.*
import net.snipsniper.utils.DropdownItem
import net.snipsniper.utils.Function
import net.snipsniper.utils.GradientJButton
import net.snipsniper.utils.IFunction
import org.capturecoop.cccolorutils.chooser.gui.CCHSBHueBar
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger.Companion.log
import org.capturecoop.cclogger.CCLogger.Companion.logStacktrace
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class EzModeSettingsCreator(private val scEditorWindow: SCEditorWindow) {
    private lateinit var stampPreviewPanel: StampJPanel
    private lateinit var lastPanel: JPanel

    fun addSettingsToPanel(panel: JPanel, stamp: IStamp, width: Int) {
        panel.removeAll()
        panel.layout = FlowLayout(FlowLayout.LEFT, 0, 0)
        panel.add(createJSeperator())
        when (stamp.type) {
            StampType.CUBE -> cube(panel, stamp, width)
            StampType.COUNTER -> counter(panel, stamp, width)
            StampType.CIRCLE -> circle(panel, stamp, width)
            StampType.SIMPLE_BRUSH -> brush(panel, stamp, width)
            StampType.TEXT -> text(panel, stamp, width)
            StampType.RECTANGLE -> rectangle(panel, stamp, width)
            StampType.ERASER -> eraser(panel, stamp, width)
        }
        panel.add(createJSeperator())
        panel.add(JLabel("preview"))
        stampPreviewPanel = StampJPanel(stamp, scEditorWindow.originalImage, 10)
        Dimension(scEditorWindow.ezModeWidth, scEditorWindow.ezModeWidth).also { dim ->
            stampPreviewPanel.preferredSize = dim
            stampPreviewPanel.minimumSize = dim
            stampPreviewPanel.maximumSize = dim
        }
        panel.add(stampPreviewPanel)
        panel.revalidate()
        panel.repaint()
        lastPanel = panel
    }

    fun lastCorrectHeight(): Int {
        var height = 0
        lastPanel.components.forEach { if(it !is CCHSBHueBar) height += it.height }
        return height
    }

    private fun addColorSettings(panel: JPanel, stamp: IStamp, width: Int) {
        val stampColor = stamp.color
        val cPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        cPanel.preferredSize = Dimension(width, 40)
        val button = GradientJButton("Color", stampColor!!)
        button.preferredSize = Dimension(width / 2, 30)
        button.addActionListener {
            scEditorWindow.openStampColorChooser()
        }
        cPanel.add(button)
        panel.add(cPanel)
        stampColor.addChangeListener { scEditorWindow.repaint() }
    }

    private fun addWidthHeightSettings(panel: JPanel, stamp: IStamp) {
        val boxMinimum = 1
        val boxMaximum = 400
        panel.add(JLabel("width"))
        val widthSlider = createEZModeSlider(boxMinimum, boxMaximum, stamp.width, object : Function() {
            override fun run(vararg args: Int): Boolean {
                stamp.width = args[0]
                stampPreviewPanel.repaint()
                return true
            }
        })
        panel.add(widthSlider)
        panel.add(createJSeperator())
        panel.add(JLabel("height"))
        val heightSlider = createEZModeSlider(boxMinimum, boxMaximum, stamp.height, object : Function() {
            override fun run(vararg args: Int): Boolean {
                stamp.height = args[0]
                stampPreviewPanel.repaint()
                return true
            }
        })
        panel.add(heightSlider)
        stamp.addChangeListener {
            if (it === IStampUpdateListener.TYPE.INPUT) {
                widthSlider.value = stamp.width
                heightSlider.value = stamp.height
            }
        }
        panel.add(createJSeperator())
    }

    private fun addBasicBoxSettings(panel: JPanel, stamp: IStamp, width: Int) {
        addWidthHeightSettings(panel, stamp)
        addColorSettings(panel, stamp, width)
    }

    private fun addBasicCircleSettings(panel: JPanel, stamp: IStamp, addColor: Boolean, width: Int) {
        panel.add(JLabel("size"))
        val sizeSlider = createEZModeSlider(1, 400, stamp.width, object : Function() {
            override fun run(vararg args: Int): Boolean {
                stamp.width = args[0]
                stamp.height = args[0]
                stampPreviewPanel.repaint()
                return true
            }
        })
        panel.add(sizeSlider)
        stamp.addChangeListener {
            if (it === IStampUpdateListener.TYPE.INPUT) sizeSlider.value = stamp.width
        }
        if (!addColor) return
        panel.add(createJSeperator())
        addColorSettings(panel, stamp, width)
    }

    private fun createEZModeSlider(min: Int, max: Int, currentValue: Int, onChange: Function): JSlider {
        return JSlider().also { slider ->
            Dimension(scEditorWindow.ezModeWidth, 30).also { dim ->
                slider.preferredSize = dim
                slider.minimumSize = dim
                slider.maximumSize = dim
            }
            slider.minimum = min
            slider.maximum = max
            slider.value = currentValue
            slider.addChangeListener {
                onChange.run(slider.value)
                stampPreviewPanel.repaint()
                scEditorWindow.requestFocus()
            }
            slider.addFocusListener(object : FocusAdapter() {
                override fun focusGained(e: FocusEvent) {
                    super.focusGained(e)
                    scEditorWindow.requestFocus()
                }
            })
        }
    }

    private fun createJSeperator(): JSeparator {
        return JSeparator().also {
            Dimension(scEditorWindow.ezModeWidth, 10).also { dim ->
                it.preferredSize = dim
                it.minimumSize = dim
                it.maximumSize = dim
            }
        }
    }

    private fun cube(panel: JPanel, stamp: IStamp, width: Int) {
        addBasicBoxSettings(panel, stamp, width)
    }

    private fun counter(panel: JPanel, stamp: IStamp, width: Int) {
        addBasicCircleSettings(panel, stamp, true, width)
    }

    private fun circle(panel: JPanel, stamp: IStamp, width: Int) {
        addBasicCircleSettings(panel, stamp, false, width)
        val cStamp = stamp as CircleStamp
        panel.add(JLabel("thickness"))
        val thicknessSlider = createEZModeSlider(1, 200, cStamp.thickness, object : Function() {
            override fun run(vararg args: Int): Boolean {
                cStamp.thickness = args[0]
                return true
            }
        })
        panel.add(thicknessSlider)
        panel.add(createJSeperator())
        stamp.addChangeListener {
            if (it === IStampUpdateListener.TYPE.INPUT) thicknessSlider.value = cStamp.thickness
        }
        addColorSettings(panel, stamp, width)
    }

    private fun brush(panel: JPanel, stamp: IStamp, width: Int) {
        addBasicCircleSettings(panel, stamp, true, width)
    }

    private fun text(panel: JPanel, stamp: IStamp, width: Int) {
        panel.add(JLabel("font size"))
        //Font size = height
        val sizeSlider = createEZModeSlider(5, 200, stamp.height, object : Function() {
            override fun run(vararg args: Int): Boolean {
                stamp.height = args[0]
                return true
            }
        })
        panel.add(sizeSlider)
        panel.add(createJSeperator())
        val textStamp = stamp as TextStamp
        panel.add(JLabel("font type"))
        val fontTypeDropdown = JComboBox<DropdownItem>()
        fontTypeDropdown.addItem(DropdownItem("plain", "plain"))
        fontTypeDropdown.addItem(DropdownItem("bold", "bold"))
        fontTypeDropdown.addItem(DropdownItem("italic", "italic"))
        val dim = Dimension(scEditorWindow.ezModeWidth, 30)
        fontTypeDropdown.minimumSize = dim
        fontTypeDropdown.maximumSize = dim
        fontTypeDropdown.preferredSize = dim
        when (textStamp.fontMode) {
            Font.PLAIN -> fontTypeDropdown.setSelectedIndex(0)
            Font.BOLD -> fontTypeDropdown.setSelectedIndex(1)
            Font.ITALIC -> fontTypeDropdown.setSelectedIndex(2)
        }

        //If we idle for more than 5 seconds remove focus
        fontTypeDropdown.addFocusListener(object : FocusAdapter() {
            override fun focusGained(focusEvent: FocusEvent) {
                super.focusGained(focusEvent)
                getNewThread(IFunction {
                    try {
                        Thread.sleep(5000)
                    } catch (ex: InterruptedException) {
                        log("Error waiting for font type dropdown in ezMode", CCLogLevel.ERROR)
                        logStacktrace(ex, CCLogLevel.ERROR)
                    }
                    scEditorWindow.requestFocus()
                }).start()
            }
        })
        fontTypeDropdown.addItemListener {
            textStamp.fontMode = fontTypeDropdown.selectedIndex
            scEditorWindow.requestFocus()
        }
        panel.add(fontTypeDropdown)
        panel.add(createJSeperator())
        panel.add(JLabel("text"))
        val textInput = JTextField()
        textInput.minimumSize = dim
        textInput.maximumSize = dim
        textInput.preferredSize = dim
        textInput.text = textStamp.text
        textInput.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(keyEvent: KeyEvent) {
                if (keyEvent.keyCode == KeyEvent.VK_ENTER || keyEvent.keyCode == KeyEvent.VK_ESCAPE) scEditorWindow.requestFocus()
            }
        })
        textInput.addFocusListener(object : FocusAdapter() {
            override fun focusGained(focusEvent: FocusEvent) {
                textInput.selectionStart = 0
                textInput.selectionEnd = textInput.text.length
            }
        })
        textInput.document.addDocumentListener(object : DocumentListener {
            fun update() = kotlin.run { textStamp.text = textInput.text }
            override fun insertUpdate(event: DocumentEvent) = update()
            override fun removeUpdate(event: DocumentEvent) = update()
            override fun changedUpdate(event: DocumentEvent) = update()
        })
        stamp.addChangeListener {
            if (it === IStampUpdateListener.TYPE.INPUT) {
                sizeSlider.value = stamp.height
                fontTypeDropdown.selectedIndex = when (textStamp.fontMode) {
                    Font.PLAIN -> 0
                    Font.BOLD -> 1
                    Font.ITALIC -> 2
                    else -> {
                        throw Exception("Bad font given ${textStamp.fontMode}")
                    }
                }
                textInput.text = textStamp.text
            }
        }
        panel.add(textInput)
        addColorSettings(panel, stamp, width)
    }

    private fun rectangle(panel: JPanel, stamp: IStamp, width: Int) {
        addWidthHeightSettings(panel, stamp)
        val rStamp = stamp as RectangleStamp
        panel.add(JLabel("thickness"))
        val thicknessSlider = createEZModeSlider(1, 200, rStamp.thickness, object : Function() {
            override fun run(vararg args: Int): Boolean {
                rStamp.thickness = args[0]
                return true
            }
        })
        panel.add(thicknessSlider)
        stamp.addChangeListener {
            if (it === IStampUpdateListener.TYPE.INPUT) thicknessSlider.value = rStamp.thickness
        }
        panel.add(createJSeperator())
        addColorSettings(panel, stamp, width)
    }

    private fun eraser(panel: JPanel, stamp: IStamp, width: Int) {
        addBasicCircleSettings(panel, stamp, false, width)
    }
}