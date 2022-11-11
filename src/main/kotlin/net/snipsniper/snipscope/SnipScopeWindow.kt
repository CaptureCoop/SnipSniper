package net.snipsniper.snipscope

import net.snipsniper.snipscope.ui.SnipScopeUIComponent
import net.snipsniper.utils.InputContainer
import net.snipsniper.utils.Utils.Companion.getScaledDimension
import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.Dimension
import java.awt.Point
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import javax.swing.JFrame

open class SnipScopeWindow : JFrame() {
    private var _image: BufferedImage? = null
    var image: BufferedImage
        get() = _image!!
        set(value) {
            _image = value
            optimalImageDimension = getScaledDimension(value, renderer.size)
        }
    var optimalImageDimension: Dimension? = null
    var position = CCVector2Int(0, 0)
    var zoomOffset = CCVector2Int(0, 0)
    private lateinit var renderer: SnipScopeRenderer
    var zoom = 1f
    val inputContainer = InputContainer()
    var movementKey = KeyEvent.VK_SPACE
    var isRequireMovementKeyForZoom = true
    val uiComponents = ArrayList<SnipScopeUIComponent>()
    var isEnableInteraction = true

    fun init(startImage: BufferedImage, renderer: SnipScopeRenderer, listener: SnipScopeListener?) {
        this.renderer = renderer
        image = startImage
        addKeyListener(listener)
        renderer.addMouseListener(listener)
        renderer.addMouseMotionListener(listener)
        renderer.addMouseWheelListener(listener)
        add(renderer)
        defaultCloseOperation = DISPOSE_ON_CLOSE
    }

    fun setSizeAuto() {
        val screenDimension = Toolkit.getDefaultToolkit().screenSize
        if (image.width >= screenDimension.getWidth() || image.height > screenDimension.getHeight()) {
            val newDimension = getScaledDimension(image, screenDimension)
            optimalImageDimension = newDimension
            size = newDimension
        } else {
            val insets = insets
            if (insets.top == 0) insets.top = height - renderer.height - insets.bottom + 1
            setSize(insets.left + insets.right + image.width, insets.bottom + insets.top + image.height)
            setLocation(location.x, location.y - insets.top)
            optimalImageDimension = Dimension(image.width, image.height)
        }
    }

    fun setLocationAuto() {
        val dimension = optimalImageDimension!!
        val screenDimension = Toolkit.getDefaultToolkit().screenSize
        setLocation(
            screenDimension.width / 2 - dimension.width / 2,
            screenDimension.height / 2 - dimension.height / 2
        )
    }

    fun calculateZoom() {
        val dimWidth = optimalImageDimension!!.width
        val dimHeight = optimalImageDimension!!.height
        val offsetX = dimWidth / 2
        val offsetY = dimHeight / 2
        val modX = (offsetX * zoom - offsetX).toInt()
        val modY = (offsetY * zoom - offsetY).toInt()
        zoomOffset = CCVector2Int(modX, modY)
        repaint()
    }

    val differenceFromImage: Array<Double>
        get() {
            val optimalDimension = optimalImageDimension!!
            val width = image.width.toDouble() / (optimalDimension.getWidth() * zoom)
            val height = image.height.toDouble() / (optimalDimension.getHeight() * zoom)
            return arrayOf(width, height)
        }

    fun getPointOnImage(point: Point?): CCVector2Int? {
        if (point == null) return null
        val optimalDimension = optimalImageDimension!!
        var imageX = renderer.width.toDouble() / 2 - optimalDimension.getWidth() / 2
        var imageY = renderer.height.toDouble() / 2 - optimalDimension.getHeight() / 2
        imageX -= zoomOffset.x.toDouble()
        imageY -= zoomOffset.y.toDouble()
        imageX -= position.x.toDouble()
        imageY -= position.y.toDouble()
        val difference = differenceFromImage
        val posOnImageX = (point.getX() - imageX) * difference[0]
        val posOnImageY = (point.getY() - imageY) * difference[1]
        return CCVector2Int(posOnImageX, posOnImageY)
    }

    fun resetZoom() {
        position = CCVector2Int()
        zoom = 1f
        zoomOffset = CCVector2Int()
    }

    open fun resizeTrigger() {
        optimalImageDimension = getScaledDimension(image, renderer.size)
        calculateZoom()
    }

    fun addUIComponent(component: SnipScopeUIComponent) = uiComponents.add(component)

    fun isPointOnUiComponents(point: Point): Boolean {
        uiComponents.forEach { if(it.contains(point)) return true }
        return false
    }
}