package net.snipsniper.utils.debug

import net.snipsniper.SnipSniper
import net.snipsniper.snipscope.SnipScopeListener
import net.snipsniper.snipscope.SnipScopeRenderer
import net.snipsniper.snipscope.SnipScopeWindow
import net.snipsniper.utils.Colors
import net.snipsniper.utils.ImageUtils
import net.snipsniper.utils.Utils
import net.snipsniper.utils.drawOutlineString
import net.snipsniper.utils.getImage
import org.capturecoop.colorcomposer.ColorUtils
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MonitorIDWindow(device: MonitorDebugWindowMonitor, timeToDieMilli: Int): JFrame() {
    init {
        isUndecorated = true
        background = Colors.TRANSPARENT
        isAlwaysOnTop = true
        isVisible = true
        setSize(device.deviceBounds.width / 4, device.deviceBounds.height / 4)
        Utils.centerWindowLocation(this, device.device)
        add(Renderer(parent = this, device = device))
        SnipSniper.getNewThread {
            Thread.sleep(timeToDieMilli.toLong())
            SwingUtilities.invokeLater { dispose() }
        }.start()
    }

    class Renderer(
        private val parent: MonitorIDWindow,
        private val device: MonitorDebugWindowMonitor
    ): JPanel() {
        private val deviceBounds: Rectangle = device.deviceBounds

        override fun paint(g: Graphics) {
            g as Graphics2D

            g.color = parent.background
            g.fillRect(0, 0, width, height)

            g.paint = GradientPaint(width / 2f, 0f, device.colorBL, width / 2f, height.toFloat(), device.colorTR)
            g.fillRoundRect(0, 0, width, height, 32, 32)

            g.font = g.font.deriveFont(deviceBounds.height / 16f)
            g.drawOutlineString(
                text = device.idString,
                x = width / 2 - (g.fontMetrics.stringWidth(device.idString) / 2),
                y = height / 2 + (g.font.size / 2),
                textColor = Color.WHITE,
                outlineColor = Color.BLACK,
                outlineThickness = g.font.size / 12f
            )
        }
    }
}

class MonitorDebugWindowMonitor(
    val device: GraphicsDevice,
    val color: Color = Colors.RANDOM_OPAQUE,
) {
    val idString: String = device.iDstring
    val displayMode: DisplayMode = device.displayMode
    val deviceBounds: Rectangle = device.defaultConfiguration.bounds
    val colorBL: Color = color.darker().darker().darker()
    val colorTR: Color = color.darker()
}

class MonitorDebugWindow: SnipScopeWindow() {
    private val topColor: Color = Color.GRAY.darker()
    private val bottomColor: Color = topColor.darker()
    private val monitorOutline: BasicStroke = BasicStroke(16f)
    private val devices: List<MonitorDebugWindowMonitor> = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.map { device ->
        MonitorDebugWindowMonitor(device = device)
    }

    private val imageMargin = 32

    init {
        title = WINDOW_TITLE
        isVisible = true
        drawOutline = false
        iconImage = "icons/monitor.png".getImage()

        init(
            startImage = getLayoutImage(),
            renderer = object: SnipScopeRenderer(this) {
                override fun paint(g: Graphics) {
                    g as Graphics2D
                    g.paint = GradientPaint(width / 2f, 0f, topColor, width / 2f, height.toFloat(), bottomColor)
                    g.fillRect(0, 0, width, height)
                    super.paint(g)
                }
            },
            listener = object: SnipScopeListener(this) {
                override fun keyReleased(keyEvent: KeyEvent) {
                    if(keyEvent.keyCode == KeyEvent.VK_I) identifyWindows()
                }
            }
        )
        focusTraversalKeysEnabled = false
        setSizeAuto()

        size = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds.size.let { size -> Dimension((size.width / 1.5).toInt(), (size.height / 1.5).toInt()) }
        Utils.centerWindowLocation(this)

        isEnableInteraction = true
        defaultCloseOperation = DISPOSE_ON_CLOSE
    }

    fun identifyWindows() {
        devices.forEach { device ->
            MonitorIDWindow(device, 2000)
        }
    }

    fun getLayoutImage(): BufferedImage {
        //TODO: This is the code used in CaptureWindow to calculate bounds
        val totalBounds: Rectangle = Rectangle2D.Double().also { rect ->
            GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.forEach { sd ->
                sd.configurations.forEach { cfg -> Rectangle2D.union(rect, cfg.bounds, rect) }
            }
        }.bounds
        return ImageUtils.newBufferedImageV2(width = totalBounds.width + (imageMargin * 2), height = totalBounds.height + (imageMargin * 2), action = { g ->
            g as Graphics2D
            g.setRenderingHints(Utils.getRenderingHints())
            g.translate(imageMargin, imageMargin)
            g.font = Font("Arial", Font.BOLD, height / 24)
            devices.forEach { device ->
                val bounds = device.deviceBounds
                g.paint = GradientPaint(
                    bounds.x.toFloat(), bounds.y.toFloat() + bounds.height, device.colorBL,
                    (bounds.x + bounds.width).toFloat(), (bounds.y).toFloat(), device.colorTR
                )
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)

                g.color = Color.BLACK
                g.stroke = monitorOutline
                g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height)

                fun drawOutlineString(text: String, x: Int, y: Int) {
                    g.drawOutlineString(text = text, x = x, y = y, textColor = Color.WHITE, outlineColor = Color.BLACK, outlineThickness = g.font.size / 6f)
                }

                val idText = "${device.idString} ${device.displayMode}"
                drawOutlineString(
                    text = idText,
                    x = bounds.x + (bounds.width / 2) - (g.fontMetrics.stringWidth(idText) / 2),
                    y = bounds.y + bounds.height - font.size - imageMargin / 2
                )

                val locStart = "(${bounds.location.x}/${bounds.location.y})"
                drawOutlineString(locStart, bounds.x + imageMargin / 2, bounds.y + g.font.size)
                val locEnd = "(${bounds.location.x + bounds.width}/${bounds.location.y + bounds.height})"
                drawOutlineString(locEnd, bounds.x + bounds.width - g.fontMetrics.stringWidth(locEnd) - imageMargin / 2, bounds.y + bounds.height - font.size- imageMargin / 2)
            }
        })
    }

    companion object {
        const val WINDOW_TITLE = "Monitor Debug View"
    }
}