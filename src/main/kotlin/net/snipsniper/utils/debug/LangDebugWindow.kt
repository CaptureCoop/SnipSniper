package net.snipsniper.utils.debug

import net.snipsniper.LangManager
import net.snipsniper.SnipSniper
import net.snipsniper.configwindow.ConfigWindow
import net.snipsniper.utils.FileUtils
import net.snipsniper.utils.Utils
import net.snipsniper.utils.getImage
import org.json.JSONObject
import java.awt.*
import javax.swing.*

class LangDebugWindow : JFrame() {
    private val scrollPane: JScrollPane
    private var currentEdit: JSONObject? = null
    private val textAreaList = ArrayList<JTextArea>()
    private val keyMap = HashMap<JTextArea, String>()
    private var lastLanguage: String? = null

    init {
        title = "Debug Language Window"
        setSize(512, 512)
        defaultCloseOperation = DISPOSE_ON_CLOSE
        iconImage = "icons/config.png".getImage()
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        setLocation(screenSize.width / 2 - width / 2, screenSize.height / 2 - height / 2)
        scrollPane = setup()
        add(scrollPane)
        resetScrollPane()
        isVisible = true
    }

    private fun setup(): JScrollPane {
        val content = JPanel()
        content.layout = GridBagLayout()
        setupLabels("en", content)
        return ConfigWindow.generateScrollPane(content)
    }

    private fun resetScrollPane() = SwingUtilities.invokeLater { scrollPane.verticalScrollBar.value = 0 }

    private fun setupLabels(language: String, content: JPanel) {
        val gbc = GridBagConstraints()
        content.removeAll()
        gbc.gridx = 0
        content.add(JLabel("English", JLabel.CENTER), gbc)
        gbc.gridx = 1
        content.add(Utils.getLanguageDropdown(language) {
            setupLabels(it, content)
            resetScrollPane()
            lastLanguage = it
        }, gbc)
        lastLanguage = "en"
        gbc.gridx = 0
        content.add(emptyPanel(), gbc)
        gbc.gridx = 1
        content.add(emptyPanel(), gbc)
        gbc.fill = GridBagConstraints.BOTH
        val enJSON = LangManager.getJSON("en")!!.getJSONObject("strings")
        currentEdit = LangManager.getJSON(language)
        enJSON.keySet().forEach {
            gbc.gridx = 0
            gbc.insets.top = 5
            gbc.insets.right = 5
            content.add(createLabel(LangManager.getItem("en", it!!), false, null), gbc)
            gbc.gridx = 1
            gbc.insets.right = 0
            gbc.insets.left = 5
            content.add(createLabel(LangManager.getItem(language, it), true, it), gbc)
            gbc.insets.right = 0
            gbc.insets.left = 0
        }
        JButton("Save").also { saveButton ->
            saveButton.addActionListener {
                for (textArea in textAreaList) {
                    val key = keyMap[textArea]
                    val toEdit = currentEdit!!.getJSONObject("strings")
                    if (toEdit.has(key)) toEdit.remove(key)
                    toEdit.put(key, textArea.text)
                }
                FileUtils.printFile(SnipSniper.mainFolder + "//" + lastLanguage + ".json", currentEdit.toString())
                FileUtils.openFolder(SnipSniper.mainFolder)
                SnipSniper.resetProfiles()
            }
            gbc.gridwidth = 2
            gbc.gridx = 0
            gbc.fill = GridBagConstraints.NONE
            content.add(saveButton, gbc)
            content.validate()
        }
    }

    private fun emptyPanel(): JPanel {
        return JPanel().also {
            it.preferredSize = Dimension(220, 10)
        }
    }

    private fun createLabel(text: String?, editable: Boolean, key: String?): JTextArea {
        return JTextArea(text).also {
            if (key != null) {
                keyMap[it] = key
                textAreaList.add(it)
            }
            it.border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK)
            it.wrapStyleWord = true
            it.lineWrap = true
            it.isEditable = editable
        }
    }
}