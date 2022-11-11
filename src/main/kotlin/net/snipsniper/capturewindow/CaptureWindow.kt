package net.snipsniper.capturewindow

import net.snipsniper.SnipSniper
import net.snipsniper.StatsManager
import net.snipsniper.config.ConfigHelper
import net.snipsniper.sceditor.SCEditorWindow
import net.snipsniper.systray.Sniper
import net.snipsniper.utils.*
import org.apache.commons.lang3.SystemUtils
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.JFrame
import kotlin.math.min
import kotlin.math.max

class CaptureWindow(val sniperInstance: Sniper) : JFrame(), WindowListener {
    val config = sniperInstance.config
    private val qualityHints = Utils.getRenderingHints()
    private val listener: CaptureWindowListener
    var screenshotBounds: Rectangle? = null
        private set
    private var screenshot: BufferedImage? = null
    private var screenshotTinted: BufferedImage? = null
    private var isRunning = true
    var isAfterDragHotkeyPressed = false
    private val dottedLineDistance: Int = config.getInt(ConfigHelper.PROFILE.dottedOutlineDistance)

    private fun loop() {
        IFunction {
            val nsPerTick = 1000000000.0 / config.getInt(ConfigHelper.PROFILE.maxFPS)
            var lastTime = System.nanoTime()
            var lastTimer = System.currentTimeMillis()
            var delta = 0.0
            var screenshotDone = false
            while (isRunning) {
                if (screenshotDone) {
                    if (!isVisible) isVisible = true
                    setSize()
                    specialRepaint()
                }
                if (screenshot != null && screenshotTinted != null && !screenshotDone) screenshotDone = true
                val now = System.nanoTime()
                delta += (now - lastTime) / nsPerTick
                lastTime = now
                while (delta >= 1) {
                    delta -= 1.0
                    if (screenshotDone) specialRepaint()
                }
                if (System.currentTimeMillis() - lastTimer >= 1000) lastTimer += 1000
            }
        }.also {
            SnipSniper.getNewThread(it).start()
        }
    }

    private fun specialRepaint() {
        if (selectArea != null) {
            val rect = selectArea!!
            val minX = min(rect.x, rect.width)
            val minY = min(rect.y, rect.height)
            val maxX = max(rect.x, rect.width)
            val maxY = max(rect.y, rect.height)
            repaint(minX, minY, maxX, maxY)
        } else {
            repaint()
        }
    }

    @Synchronized
    fun screenshot() {
        StatsManager.incrementCount(StatsManager.SCREENSHOTS_TAKEN_AMOUNT)
        screenshotBounds = totalBounds
        try {
            screenshot = Robot().createScreenCapture(
                Rectangle(
                    screenshotBounds!!.x,
                    screenshotBounds!!.y,
                    screenshotBounds!!.width,
                    screenshotBounds!!.height
                )
            )
        } catch (exception: AWTException) {
            CCLogger.error("Couldn't take screenshot. Message:")
            CCLogger.logStacktrace(exception, CCLogLevel.ERROR)
        }
        screenshotTinted = screenshot!!.clone()
        val g2 = screenshotTinted!!.graphics
        g2.color = config.getColor(ConfigHelper.PROFILE.tintColor).primaryColor
        g2.fillRect(0, 0, screenshotTinted!!.width, screenshotTinted!!.height)
        g2.dispose()
    }

    fun setSize() {
        setLocation(screenshotBounds!!.x, screenshotBounds!!.y)
        setSize(screenshotBounds!!.width, screenshotBounds!!.height)
        requestFocus()
        isAlwaysOnTop = true
        repaint()
    }

    fun calcRectangle(): Rectangle {
        var minX = 0; var maxX = 0
        var minY = 0; var maxY = 0
        val startPoint = listener.getStartPoint(PointType.NORMAL)
        val cPoint = listener.getCurrentPoint(PointType.NORMAL)
        if (startPoint != null && cPoint != null) {
            minX = min(startPoint.x, cPoint.x)
            maxX = max(startPoint.x, cPoint.x)
            minY = min(startPoint.y, cPoint.y)
            maxY = max(startPoint.y, cPoint.y)
        }
        return Rectangle(minX, minY, maxX - minX, maxY - minY)
    }

    private val totalBounds: Rectangle
        get() {
            val result = Rectangle2D.Double()
            val localGE = GraphicsEnvironment.getLocalGraphicsEnvironment()
            for (gd in localGE.screenDevices) {
                for (graphicsConfiguration in gd.configurations) {
                    Rectangle2D.union(result, graphicsConfiguration.bounds, result)
                }
            }
            return result.bounds
        }

    fun capture(saveOverride: Boolean, copyOverride: Boolean, editorOverride: Boolean, enforceOverride: Boolean) {
        val finalImg: BufferedImage
        isRunning = false
        dispose()
        val borderSize = config.getInt(ConfigHelper.PROFILE.borderSize)
        val captureArea = calcRectangle()
        if (captureArea.width == 0 || captureArea.height == 0) {
            sniperInstance.alert("Error: Screenshot width or height is 0!", "ERROR", TrayIcon.MessageType.ERROR)
            sniperInstance.killCaptureWindow()
            return
        }
        if (captureArea.x < 0) captureArea.x = 0
        if (captureArea.y < 0) captureArea.y = 0
        if (captureArea.width + captureArea.x > screenshotBounds!!.width) captureArea.width =
            screenshotBounds!!.width - captureArea.x
        if (captureArea.height + captureArea.y > screenshotBounds!!.height) captureArea.height =
            screenshotBounds!!.height - captureArea.y
        val croppedBuffer =
            screenshot!!.getSubimage(captureArea.x, captureArea.y, captureArea.width, captureArea.height)
        finalImg = BufferedImage(croppedBuffer.width + borderSize * 2, croppedBuffer.height + borderSize * 2, BufferedImage.TYPE_INT_ARGB)
        (finalImg.graphics as Graphics2D).also { g ->
            g.paint = config.getColor(ConfigHelper.PROFILE.borderColor).getGradientPaint(finalImg.width, finalImg.height)
            g.fillRect(0, 0, finalImg.width, finalImg.height)
            g.drawImage(croppedBuffer, borderSize, borderSize, croppedBuffer.width, croppedBuffer.height, this)
            g.dispose()
        }
        var finalLocation: String? = null
        var inClipboard = false
        if (config.getBool(ConfigHelper.PROFILE.saveToDisk) || saveOverride) {
            if (!enforceOverride || saveOverride) {
                finalLocation = ImageUtils.saveImage(finalImg, config.getString(ConfigHelper.PROFILE.saveFormat), "", config)
                if (finalLocation != null) {
                    val folder = finalLocation.replace(File(finalLocation).name, "")
                    config.set(ConfigHelper.PROFILE.lastSaveFolder, folder)
                    config.save()
                }
            }
        }
        if (config.getBool(ConfigHelper.PROFILE.copyToClipboard) || copyOverride) {
            if (!enforceOverride || copyOverride) {
                finalImg.copyToClipboard()
                inClipboard = true
            }
        }
        if (config.getBool(ConfigHelper.PROFILE.openEditor) || editorOverride) {
            if (!enforceOverride || editorOverride) {
                val startPointTotal = listener.getStartPoint(PointType.TOTAL)
                val cPointTotal = listener.getCurrentPoint(PointType.TOTAL)
                var posX = cPointTotal!!.x
                var posY = cPointTotal.y
                var leftToRight = false
                if (startPointTotal!!.x <= cPointTotal.x) {
                    posX -= finalImg.width
                    leftToRight = true
                }
                if (startPointTotal.y <= cPointTotal.y) {
                    posY -= finalImg.height
                    leftToRight = true
                }
                SCEditorWindow(finalImg, posX, posY, "SnipSniper Editor", config, leftToRight, finalLocation, inClipboard, false)
            }
        }
        sniperInstance.killCaptureWindow()
    }

    private var selectArea: Rectangle? = null
    private var hasSaved = false
    private var globalBufferImage: BufferedImage? = null
    private var selectBufferImage: BufferedImage? = null
    private var spyglassBufferImage: BufferedImage? = null
    private val allBounds = RectangleCollection()
    private var lastRect: Rectangle? = null
    private var spyglassToggle = false

    init {
        if (sniperInstance.config.getInt(ConfigHelper.PROFILE.snipeDelay) != 0) {
            try {
                Thread.sleep(sniperInstance.config.getInt(ConfigHelper.PROFILE.snipeDelay) * 1000L)
            } catch (e: InterruptedException) {
                CCLogger.error("There was an error with the delay! Message: ${e.message}")
                CCLogger.error("More info: ${e.stackTrace.contentToString()}")
            }
        }
        screenshot()
        isUndecorated = true
        iconImage = "icons/snipsniper.png".getImage()
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        listener = CaptureWindowListener(this)
        addWindowListener(this)
        addMouseListener(listener)
        addMouseMotionListener(listener)
        addKeyListener(listener)
        addFocusListener(object : FocusAdapter() {
            override fun focusLost(focusEvent: FocusEvent) {
                setSize()
            }
        })
        isVisible = true
        setSize()
        if (SystemUtils.IS_OS_LINUX) GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.fullScreenWindow =
            this
        loop()
    }

    override fun paint(g: Graphics) {
        val directDraw = config.getBool(ConfigHelper.PROFILE.directDraw)
        //TODO: Direct draw runs horribly on linux. Check out why?
        if (lastRect == null) lastRect = screenshotBounds
        if (!directDraw && screenshotBounds != null && globalBufferImage == null && selectBufferImage == null) {
            //We are only setting this once, since the size of bounds should not really change
            globalBufferImage =
                BufferedImage(screenshotBounds!!.width, screenshotBounds!!.height, BufferedImage.TYPE_INT_RGB)
            selectBufferImage =
                BufferedImage(screenshotBounds!!.width, screenshotBounds!!.height, BufferedImage.TYPE_INT_RGB)
        }
        if (spyglassBufferImage == null && config.getBool(ConfigHelper.PROFILE.enableSpyglass)) {
            spyglassBufferImage = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)
        }
        var globalBuffer = globalBufferImage!!.graphics as Graphics2D
        globalBuffer.setRenderingHints(qualityHints)
        var selectBuffer = selectBufferImage!!.graphics as Graphics2D
        selectBuffer.setRenderingHints(qualityHints)
        var spyglassBuffer: Graphics2D? = null
        if (spyglassBufferImage != null) {
            spyglassBuffer = spyglassBufferImage!!.graphics as Graphics2D
            spyglassBuffer.setRenderingHints(qualityHints)
        }
        if (directDraw) {
            globalBuffer = g as Graphics2D
            selectBuffer = g
            spyglassBuffer = g
        }
        if (!allBounds.isEmpty()) {
            allBounds.getBounds().also { r ->
                globalBuffer.drawImage(screenshotTinted, r.x, r.y, r.width, r.height, r.x, r.y, r.width, r.height, this)
            }
        }
        allBounds.clear()
        if (screenshot != null) {
            if (screenshotTinted != null && !hasSaved && screenshotBounds != null || SystemUtils.IS_OS_LINUX) {
                CCLogger.debug("About to render image: $screenshotTinted")
                CCLogger.debug("Frame Visible: $isVisible")
                globalBuffer.drawImage(screenshotTinted, 0, 0, screenshotBounds!!.width, screenshotBounds!!.height, this)
                allBounds.addRectangle(screenshotBounds!!)
                CCLogger.debug("Rendered tinted background. More Info: ")
                CCLogger.debug("Image rendered: $screenshotTinted")
                CCLogger.debug("Frame Visible: $isVisible")
                hasSaved = true
            }
            val cPoint = listener.getCurrentPoint(PointType.NORMAL)
            val cPointLive = listener.getCurrentPoint(PointType.LIVE)
            val startPoint = listener.getStartPoint(PointType.NORMAL)
            if (selectArea != null && cPoint != null && listener.startedCapture()) {
                selectBuffer.drawImage(screenshot, startPoint!!.x, startPoint.y, cPoint.x, cPoint.y, startPoint.x, startPoint.y, cPoint.x, cPoint.y, this)
            }
            if (cPoint != null && startPoint != null) {
                selectArea = Rectangle(startPoint.x, startPoint.y, cPoint.x, cPoint.y)
                allBounds.addRectangle(Utils.fixRectangle(selectArea!!))
            }
            if (cPoint != null && selectArea != null) {
                val sa = selectArea!!
                globalBuffer.drawImage(selectBufferImage, sa.x, sa.y, sa.width, sa.height, sa.x, sa.y, sa.width, sa.height, this)
            }
            if (config.getBool(ConfigHelper.PROFILE.dottedOutline) && cPoint != null && startPoint != null && selectArea != null) {
                val thickness = 1
                val rec = Utils.fixRectangle(selectArea!!)
                allBounds.addRectangle(Rectangle(rec.x - thickness, rec.y - thickness, rec.width + thickness * 2, rec.height + thickness * 2))
                drawDashedLine(globalBuffer, rec.x - thickness, rec.y - thickness, rec.width, rec.y - thickness, thickness)
                drawDashedLine(globalBuffer, rec.x - thickness, rec.y, rec.x - thickness, rec.height, thickness)
                drawDashedLine(globalBuffer, rec.width, rec.y, rec.width, rec.height, thickness)
                drawDashedLine(globalBuffer, rec.x, rec.height, rec.width, rec.height, thickness)
            }
            if (cPointLive != null && config.getBool(ConfigHelper.PROFILE.enableSpyglass)) {
                var displaySpyglass = true
                val sgHotkey = config.getInt(ConfigHelper.PROFILE.spyglassHotkey)
                when (config.getString(ConfigHelper.PROFILE.spyglassMode)) {
                    "hold" -> if (!listener.isPressed(sgHotkey)) displaySpyglass = false
                    "toggle" -> {
                        if (listener.isPressedOnce(sgHotkey)) spyglassToggle = !spyglassToggle
                        displaySpyglass = spyglassToggle
                    }
                }
                if (displaySpyglass) {
                    val cpl = cPointLive
                    val sgbi = spyglassBufferImage ?: throw Exception("spyglassBufferImage is null!")
                    var sgrect: Rectangle? = null
                    val localGE = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    for (gd in localGE.screenDevices) {
                        for (graphicsConfiguration in gd.configurations) {
                            val rect = graphicsConfiguration.bounds
                            val point = MouseInfo.getPointerInfo().location
                            if (rect.contains(point)) {
                                sgrect = if (point.x - sgbi.width < rect.x) {
                                    Rectangle(cpl.x, cpl.y - sgbi.height, cpl.x + sgbi.width, cpl.y)
                                } else Rectangle(cpl.x - sgbi.width, cpl.y - sgbi.height, cpl.x, cpl.y)

                                if (point.y - sgbi.height < rect.y)
                                    sgrect = Rectangle(sgrect.x, cpl.y, sgrect.width, cpl.y + sgbi.height)
                            }
                        }
                    }
                    if (sgrect != null) {
                        generateSpyglass(sgbi)
                        val oldClip = globalBuffer.clip
                        val shape = Ellipse2D.Double(sgrect.x.toDouble(), sgrect.y.toDouble(), sgbi.width.toDouble(), sgbi.height.toDouble())
                        globalBuffer.clip = shape
                        globalBuffer.drawImage(sgbi, sgrect.x, sgrect.y, this)
                        globalBuffer.clip = oldClip
                        allBounds.addRectangle(sgrect)
                    }
                    if (sgrect != null) {
                        val positionText = "X: ${cPointLive.x} Y: ${cPointLive.y}"
                        val positionTextRect = globalBuffer.font.getStringBounds(positionText, globalBuffer.fontRenderContext).bounds
                        val pointX = sgrect.x + sgbi.width / 2 - positionTextRect.width / 2
                        val pointY = sgrect.y + sgbi.height + positionTextRect.height
                        globalBuffer.drawString(positionText, pointX, pointY)
                        allBounds.addRectangle(Rectangle(pointX, pointY, positionTextRect.width + pointX, positionTextRect.height + pointY))
                    }
                }
            }
            if (lastRect != null) {
                val lr = lastRect!!
                g.drawImage(globalBufferImage, lr.x, lr.y, lr.width, lr.height, lr.x, lr.y, lr.width, lr.height, this)
                lastRect = allBounds.getBounds()
            }
        } else {
            CCLogger.warn("WARNING: Screenshot is null when trying to render. Trying again.")
            repaint()
        }
        globalBuffer.dispose()
        selectBuffer.dispose()
        spyglassBuffer?.dispose()
    }

    private fun drawDashedLine(g: Graphics, x1: Int, y1: Int, x2: Int, y2: Int, thickness: Int) {
        val g2d = g.create() as Graphics2D
        g2d.color = Color.BLACK
        g2d.drawLine(x1, y1, x2, y2)
        val dashed: Stroke = BasicStroke(thickness.toFloat(), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0f, floatArrayOf(dottedLineDistance.toFloat()), 0f)
        g2d.stroke = dashed
        g2d.color = Color.WHITE
        g2d.drawLine(x1, y1, x2, y2)
        g2d.dispose()
    }

    private fun generateSpyglass(image: BufferedImage?) {
        val ROWS = config.getInt(ConfigHelper.PROFILE.spyglassZoom)
        val THICKNESS = config.getInt(ConfigHelper.PROFILE.spyglassThickness)
        if (config.getBool(ConfigHelper.PROFILE.spyglassPixelByPixel))
            generateSpyglassPixelByPixel(image, ROWS, THICKNESS)
        else generateSpyglassDirect(image!!, ROWS, THICKNESS)
    }

    private fun generateSpyglassDirect(image: BufferedImage, rows: Int, thickness: Int) {
        val g = image.graphics as Graphics2D
        val cPointLive = listener.getCurrentPoint(PointType.LIVE)
        val cpl = cPointLive!!
        fun m(p: Int) = p - rows / 2
        fun p(p: Int) = p + rows / 2
        g.drawImage(globalBufferImage, 0, 0, image.width, image.height,  m(cpl.x), m(cpl.y), p(cpl.x), p(cpl.y), this)
        g.color = Color.BLACK
        val space = image.width / rows
        for (i in 0 until rows) {
            g.drawLine(i * space, 0, i * space, image.height)
            g.drawLine(0, i * space, image.width, i * space)
        }
        generateSpyglassStroke(g, image, thickness)
    }

    private fun generateSpyglassPixelByPixel(image: BufferedImage?, rows: Int, thickness: Int) {
        val g = image!!.graphics as Graphics2D
        val ROW_SIZE = image.width / rows
        val cPointLive = listener.getCurrentPoint(PointType.LIVE)!!
        g.setRenderingHints(qualityHints)
        g.fillRect(0, 0, image.width, image.height)
        for (y in 0 until rows) {
            for (x in 0 until rows) {
                val rect = Rectangle(x * ROW_SIZE, y * ROW_SIZE, ROW_SIZE, ROW_SIZE)
                val pixelX = cPointLive.x + x - rows / 2
                val pixelY = cPointLive.y + y - rows / 2
                if (pixelX < globalBufferImage!!.width && pixelY < globalBufferImage!!.height && pixelX >= 0 && pixelY >= 0) {
                    g.color = Color(globalBufferImage!!.getRGB(pixelX, pixelY))
                    g.fillRect(rect.x, rect.y, rect.width, rect.height)
                }
                g.color = Color.BLACK
                g.drawRect(rect.x, rect.y, rect.width, rect.height)
            }
        }
        generateSpyglassStroke(g, image, thickness)
        g.dispose()
    }

    private fun generateSpyglassStroke(g: Graphics2D, image: BufferedImage?, thickness: Int) {
        val oldStroke = g.stroke
        g.stroke = BasicStroke(thickness.toFloat())
        g.drawLine(image!!.width / 2, 0, image.width / 2, image.height)
        g.drawLine(0, image.height / 2, image.width, image.height / 2)
        g.stroke = BasicStroke((thickness * 2).toFloat())
        g.drawOval(0, 0, image.width, image.height)
        g.stroke = oldStroke
    }

    override fun windowActivated(windowEvent: WindowEvent) {}
    override fun windowClosed(windowEvent: WindowEvent) {}
    override fun windowClosing(windowEvent: WindowEvent) = sniperInstance.killCaptureWindow()

    override fun windowDeactivated(windowEvent: WindowEvent) {}
    override fun windowDeiconified(windowEvent: WindowEvent) {}
    override fun windowIconified(windowEvent: WindowEvent) {}
    override fun windowOpened(windowEvent: WindowEvent) {}
    val isAfterDragEnabled: Boolean
        get() {
            if (afterDragMode.equals("none", true)) return false
            return if (afterDragMode.equals("enabled", true)) true
            else afterDragMode.equals("hold", true) && isAfterDragHotkeyPressed
        }
    val afterDragMode: String
        get() = config.getString(ConfigHelper.PROFILE.afterDragMode)
    val afterDragHotkey: Int
        get() = config.getInt(ConfigHelper.PROFILE.afterDragHotkey)
}