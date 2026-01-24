package net.snipsniper.utils

import net.snipsniper.SnipSniper
import net.snipsniper.StatsManager
import net.snipsniper.config.ConfigHelper
import net.snipsniper.secrets.games.BGame
import net.snipsniper.systray.Sniper
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets
import javax.swing.*
import javax.swing.event.ChangeListener
import javax.swing.event.HyperlinkEvent
import javax.swing.text.Caret
import javax.swing.text.JTextComponent

class AboutWindow(private val sniper: Sniper): JFrame() {
    private lateinit var html: String
    private var onC = false
    private val instance = this

    init {
        loadHTML()
        size = Dimension(512, 256)
        title = "About"
        isResizable = true
        iconImage = "icons/snipsniper.png".getImage()
        //JPanel panel = new JPanel(new BorderLayout());
        //panel.add(leftComponent, BorderLayout.WEST);  // stays minimal
        //panel.add(rightComponent, BorderLayout.CENTER); // fills remaining space
        JPanel(BorderLayout()).also { mainPanel ->
            val mainGbc = GridBagConstraints()
            val iconPanel = JPanel(GridBagLayout())
            val gbc = GridBagConstraints()
            gbc.gridx = 0
            gbc.gridy = 0
            val iconSize = SnipSniper.calculateEffectiveUIScale(100)
            val icon = "icons/snipsniper.png".getImage().scaledSmooth(iconSize, iconSize).toImageIcon()
            JLabel(icon).also{ iconLabel ->
                iconLabel.addMouseListener(object: MouseAdapter() {
                    var index = 0
                    val icons = arrayOf("icons/snipsniper.png".getImage(), "icons/editor.png".getImage(), "icons/viewer.png".getImage(), "icons/console.png".getImage())
                    val cache = HashMap<String, Image>()

                    override fun mouseReleased(mouseEvent: MouseEvent) {
                        super.mouseReleased(mouseEvent)
                        if(index >= icons.size - 1) index = 0
                        else index++
                        onC = index == 3
                        setNewImage(index, iconSize, true)
                        StatsManager.incrementCount(StatsManager.ABOUT_ICON_CLICKED_AMOUNT)
                    }

                    override fun mousePressed(mouseEvent: MouseEvent) {
                        setNewImage(index, (iconSize / 1.2F).toInt(), false)
                    }

                    fun setNewImage(index: Int, size: Int, replaceTaskbar: Boolean) {
                        val image: Image
                        val key = "${index}_$size" //We cache those because we really like clicking the icons really fast :^)
                        if(cache.containsKey(key)) {
                            image = cache[key] ?: throw Exception("Cached image is null $key")
                        } else {
                            image = resizeImageButRetainSize(icons[index], iconSize, size)
                            cache[key] = image
                        }
                        iconLabel.icon = image.toImageIcon()
                        if(replaceTaskbar) iconImage = image
                    }
                })
                iconPanel.add(iconLabel, gbc)
            }
            gbc.gridy = 1
            gbc.insets = Insets(20, 0, 0, 0)
            JButton("Buy us a coffee").also { buyCoffee ->
                val coffeeIcon = "icons/coffee.gif".getAnimatedImage()
                val scale = (16 / SnipSniper.getEffectiveUIScale()).toInt()
                buyCoffee.icon = coffeeIcon.scaled(coffeeIcon.getWidth(null) / scale, coffeeIcon.getHeight(null) / scale).toImageIcon()
                buyCoffee.horizontalTextPosition = SwingConstants.LEFT
                buyCoffee.isFocusable = false
                iconPanel.add(buyCoffee, gbc)
            }
            mainGbc.gridx = 0
            mainGbc.weightx = 0.0
            mainGbc.fill = GridBagConstraints.NONE
            mainPanel.add(JPanel(BorderLayout()).apply {
                setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32))
                add(iconPanel)
            }, BorderLayout.WEST)

            JPanel(GridLayout(2, 0)).also { rightSide ->
                val splash = "splash.png".getImage()
                val splashLabel = JLabel(splash.scaled((splash.width / 2.2F).toInt(), (splash.height / 2.2F).toInt()).scaled(SnipSniper.getEffectiveUIScale()).toImageIcon())
                splashLabel.addMouseListener(object: MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        super.mouseClicked(e)
                        if(SnipSniper.platformType == PlatformType.JAR && onC) {
                            val channel = when(val rt = Utils.getReleaseType(SnipSniper.config.getString(ConfigHelper.MAIN.updateChannel))) {
                                ReleaseType.STABLE -> ReleaseType.DEV
                                ReleaseType.DEV -> ReleaseType.STABLE
                                else -> { throw Exception("AboutWindow: ReleaseType had bad value $rt") }
                            }

                            SnipSniper.config.set(ConfigHelper.MAIN.updateChannel, channel.toString())
                            SnipSniper.config.save()
                            Utils.showPopup(instance, "New update channel: $channel", "Channel unlocked!", JOptionPane.DEFAULT_OPTION, JOptionPane.DEFAULT_OPTION, "icons/checkmark.png".getImage(), true)
                        }
                    }
                })
                rightSide.add(splashLabel)

                JEditorPane("text/html", html).also { about ->
                    about.caret = DummyCaret()
                    about.isEditable = false
                    about.isOpaque = false
                    var secretCount = 0
                    about.addHyperlinkListener { hle ->
                        if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.eventType)) {
                            if(hle.description.equals("secret")) {
                                if(secretCount >= 10) {
                                    BGame(sniper)
                                    secretCount = 0
                                } else {
                                    secretCount++
                                }
                            } else {
                                Links.openLink(hle.url.toString())
                            }
                        }
                    }
                    rightSide.add(about)
                }
                mainGbc.gridx = 1
                mainGbc.weightx = 1.0
                mainGbc.fill = GridBagConstraints.BOTH
                mainPanel.add(rightSide, BorderLayout.CENTER)
            }

            add(mainPanel)
            Toolkit.getDefaultToolkit().screenSize.also {
                setLocation(it.width / 2 - width / 2, it.height / 2 - height / 2)
            }
            isVisible = true
            pack()
        }
    }

    private fun loadHTML() {
        html = StringBuilder().also { sb ->
            //Streams
            val inputStream = ClassLoader.getSystemResourceAsStream("net/snipsniper/resources/about.html") ?: throw FileNotFoundException("Could not load about.html inside jar!")
            val streamReader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            //Read file
            BufferedReader(streamReader).readLines().forEach { sb.append(it) }
            //Close streams
            inputStream.close()
            streamReader.close()
        }.toString()

        SnipSniper.buildInfo.also { bi ->
            html = html.replace("%VERSION%", bi.version.toString())
            html = html.replace("%BUILDDATE%", bi.buildDate)
            html = html.replace("%HASH%", bi.gitHash)
        }
        html = html.replace("%ABOUT_PROGRAMMING%", "about_programming".translate())
        html = html.replace("%ABOUT_CD%", "about_cd".translate())
        html = html.replace("%ABOUT_MATH%", "about_math".translate())
        val color = when(val theme = SnipSniper.config.getString(ConfigHelper.MAIN.theme)) {
            "dark" -> "white"
            "light" -> "black"
            else -> { throw Exception("AboutWindow: Bad value for theme: $theme") }
        }
        html = html.replace("%TEXT_COLOR%", color)
    }

    private fun resizeImageButRetainSize(image: BufferedImage, oldSize: Int, newSize: Int) = ImageUtils.newBufferedImage(oldSize, oldSize) {
        val difference = (oldSize - newSize) / 2
        it.drawImage(image.scaled(newSize, newSize), difference, difference, null)
    }
}