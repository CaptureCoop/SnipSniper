package net.snipsniper.scviewer

import net.snipsniper.SnipSniper
import net.snipsniper.StatsManager
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.sceditor.SCEditorWindow
import net.snipsniper.snipscope.SnipScopeWindow
import net.snipsniper.utils.*
import org.apache.commons.lang3.SystemUtils
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class SCViewerWindow(private var file: File?, private var config: Config?, isStandalone: Boolean): SnipScopeWindow() {
    private val files = ArrayList<String>()
    private val extensions = listOf("png", "jpg", "jpeg")
    private var locked = false
    private lateinit var saveItem: JMenuItem
    private lateinit var defaultImage: BufferedImage
    //TODO: Loads of reusing of already taken names here, works fine but probably not good practice
    // eg: image, file etc are used by SnipScopeWindow and this class too
    init {
        StatsManager.incrementCount(StatsManager.VIEWER_STARTED_AMOUNT)
        defaultCloseOperation = DISPOSE_ON_CLOSE
        refreshTitle()
        iconImage = "icons/viewer.png".getImage()

        val image: BufferedImage
        if(file != null) {
            refreshFolder()
            image = getImageFromFile(file!!) ?: throw Exception("Bad image get!")
        } else {
            image = ImageUtils.getDragPasteImage("icons/viewer.png".getImage(), "Drop image here!")
            defaultImage = image
        }

        if(config == null) {
            config = Config("viewer.cfg", "profile_defaults.cfg")
            config?.save()
        }

        isRequireMovementKeyForZoom = false
        val listener = SCViewerListener(this)
        val renderer = SCViewerRenderer(this)
        renderer.dropTarget = object: DropTarget() {
            override fun drop(evt: DropTargetDropEvent) {
                evt.acceptDrop(DnDConstants.ACTION_COPY)
                val droppedFiles = evt.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                setImage(droppedFiles[0] as File)
            }
        }

        init(image, renderer , listener)

        isVisible = true
        setSizeAuto()
        setLocationAuto()
        if(config!!.getBool(ConfigHelper.PROFILE.openViewerInFullscreen))
            extendedState = MAXIMIZED_BOTH

        if(SystemUtils.IS_OS_WINDOWS) {
            JMenuBar().also { menu ->
                val rotate = "icons/restart.png".getImage().scaled(16, 16)
                JMenuItem().also {
                    it.icon = rotate.toImageIcon()
                    it.addActionListener { rotateImage(ClockDirection.COUNTERCLOCKWISE) }
                    menu.add(it)
                }
                JMenuItem().also {
                    it.icon = rotate.flipHorizontally().toImageIcon()
                    it.addActionListener { rotateImage(ClockDirection.CLOCKWISE) }
                    menu.add(it)
                }

                saveItem = JMenuItem("Save")
                saveItem.addActionListener {
                    ImageIO.write(image, "png", file)
                    saveItem.isEnabled = false
                }
                menu.add(saveItem)
                saveItem.isEnabled = false
                jMenuBar = menu
            }
        }

        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                super.windowClosing(e)
                if(isStandalone) SnipSniper.exit(false)
                dispose()
            }
        })
        isEnableInteraction = !isDefaultImage()
    }

    private fun rotateImage(direction: ClockDirection) {
        image = when(direction) {
            ClockDirection.CLOCKWISE -> ImageUtils.rotateClockwise90(image)
            ClockDirection.COUNTERCLOCKWISE -> ImageUtils.rotateClockwise90(image, 3)
        }
        if(file != null) saveItem.isEnabled = true
        repaint()
    }

    private fun refreshTitle() {
        title = "SnipSniper Viewer"
        if(file != null) title += " (${file?.absolutePath})"
    }

    fun refreshFolder() {
        files.clear()
        File(file!!.absolutePath.replace(file!!.name, "")).listFiles()?.forEach {
            if(extensions.contains(it.extension.lowercase()))
                files.add(it.absolutePath)
        }
    }

    fun slideImage(direction: Int) {
        if(locked) return
        locked = true
        var index = files.indexOf(file?.absolutePath)
        if(direction == -1) {
            if(index > 0) index--
            else index = files.size - 1
        } else if(direction == 1) {
            if(index < files.size - 1) index++
            else index = 0
        }
        File(files[index]).also { newFile ->
            if(!file?.absolutePath.equals(newFile.absolutePath))
                setImage(newFile)
        }
        locked = false
        resetZoom()
    }

    fun openEditor() {
        if(file == null) return
        SCEditorWindow(image, location.x, location.y, "SnipSniper Editor", config!!, false, file?.absolutePath, false, false).also { it.size = size }
        if (config!!.getBool(ConfigHelper.PROFILE.closeViewerOnOpenEditor))
            dispose()
    }

    fun setImage(file: File) {
        super.image = ImageIcon(file.absolutePath).image.toBufferedImage()
        this.file = File(file.absolutePath)
        isEnableInteraction = !isDefaultImage()
        refreshTitle()
        refreshFolder()
        repaint()
    }

    private fun getImageFromFile(file: File): BufferedImage? {
        if(!file.exists() || !extensions.contains(file.extension)) return null
        image = ImageIO.read(file)
        return image
    }

    fun isDefaultImage() = image == defaultImage
}