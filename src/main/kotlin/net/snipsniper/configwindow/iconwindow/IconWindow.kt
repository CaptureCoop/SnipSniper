package net.snipsniper.configwindow.iconwindow

import net.snipsniper.ImageManager
import net.snipsniper.SnipSniper
import net.snipsniper.utils.*
import org.capturecoop.ccutils.utils.CCIClosable
import org.capturecoop.ccutils.utils.CCStringUtils
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Rectangle
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileFilter

class IconWindow(title: String, parent: JFrame, private val onSelectIcon: IFunction): JFrame(), CCIClosable {
    enum class ICON_TYPE {GENERAL, RANDOM, CUSTOM}
    private val rows = 4

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        size = Dimension(512, 256)
        iconImage = "icons/folder.png".getImage()
        this.title = title
        setLocation(parent.location.x + parent.width / 2 - width / 2, parent.location.y + parent.height / 2 - height / 2)
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(windowEvent: WindowEvent) {
                super.windowClosing(windowEvent)
                close()
            }
        })

        //TODO: This listener doesnt work?
        addMouseListener(object: MouseAdapter() {
            var listenForExit = false
            override fun mouseEntered(mouseEvent: MouseEvent) = kotlin.run { listenForExit = true }
            override fun mouseExited(mouseEvent: MouseEvent) {
                if(listenForExit && !Rectangle(0, 0, bounds.width, bounds.height).contains(mouseEvent.point))
                    dispose()
            }
        })

        isResizable = false
        isVisible = true
        JTabbedPane().also {
            it.addTab("General", setupPanel(ICON_TYPE.GENERAL))
            it.addTab("Random", setupPanel(ICON_TYPE.RANDOM))
            it.addTab("Custom", setupPanel(ICON_TYPE.CUSTOM))
            add(it)
        }
        pack()
        setSize(width, 256) //TODO: Why?
    }

    private fun setupPanel(type: ICON_TYPE): JScrollPane {
        JPanel(GridBagLayout()).also { content ->
            popuplateButtons(content, type)
            return JScrollPane(content).also { scrollPane ->
                scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
                scrollPane.border = BorderFactory.createEmptyBorder()
                scrollPane.verticalScrollBar.unitIncrement = 20
            }
        }
    }

    fun popuplateButtons(content: JPanel, type: ICON_TYPE) {
        content.removeAll()
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.gridx = 0
        val list = ArrayList<SSFile>()
        ImageManager.filenameList.forEach { file ->
            if (type == ICON_TYPE.GENERAL && file.contains("icons") && !file.contains("icons/random/"))
                list.add(SSFile(file, SSFile.LOCATION.JAR))
            if (type == ICON_TYPE.RANDOM && file.contains("icons/random/"))
                list.add(SSFile(file, SSFile.LOCATION.JAR))
        }
        if (type == ICON_TYPE.CUSTOM) {
            File(SnipSniper.imgFolder).walk().filter { it.isFile }.forEach { localFile ->
                list.add(SSFile(localFile.name, SSFile.LOCATION.LOCAL))
            }
        }
        val size = rootPane.width / 5
        val sizeDim = Dimension(size, size)
        list.forEach { file ->
            IconButton(file.getPathWithLocation(), file.location).also { btn ->
                btn.onSelect = IFunction {
                    onSelectIcon.run(btn.id)
                    dispose()
                }
                btn.onDelete = IFunction { popuplateButtons(content, type) }
                when (file.location) {
                    SSFile.LOCATION.JAR -> {
                        if (file.path.endsWith(".png"))
                            btn.icon = file.path.getImage().scaled(size, size).toImageIcon()
                        else if (file.path.endsWith(".gif"))
                            btn.icon = file.path.getAnimatedImage().scaled(size, size).toImageIcon()
                    }

                    SSFile.LOCATION.LOCAL -> btn.icon = ImageIcon(
                        ImageUtils.getImageFromDisk("${SnipSniper.imgFolder}/${file.path}").scaledSmooth(size, size)
                    )
                }
                content.add(btn, gbc)
                gbc.gridx++
                if (gbc.gridx >= rows) gbc.gridx = 0
            }
        }
        if (type == ICON_TYPE.CUSTOM) {
            content.dropTarget = object : DropTarget() {
                override fun drop(evt: DropTargetDropEvent) {
                    evt.acceptDrop(DnDConstants.ACTION_COPY)
                    (evt.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>).forEach {
                        it as File
                        loadFile(it)
                        popuplateButtons(content, type)
                    }
                }
            }
            JButton("New").also { newBtn ->
                newBtn.preferredSize = sizeDim
                newBtn.minimumSize = sizeDim
                newBtn.maximumSize = sizeDim
                newBtn.addActionListener {
                    JFileChooser().also { chooser ->
                        val filter = object : FileFilter() {
                            override fun accept(pathname: File): Boolean {
                                if (pathname.isDirectory) return true
                                return CCStringUtils.endsWith(
                                    pathname.name.lowercase(),
                                    ".png",
                                    ".gif",
                                    ".jpg",
                                    ".jpeg"
                                )
                            }

                            override fun getDescription() = "Images"
                        }
                        chooser.addChoosableFileFilter(filter)
                        chooser.fileFilter = filter
                        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                            loadFile(chooser.selectedFile)
                            popuplateButtons(content, type)
                        }
                    }
                }
                content.add(newBtn, gbc)
            }
        }
        content.revalidate()
        content.repaint()
    }

    fun loadFile(file: File) {
        //We use smooth Scaling for everything but gifs
        if(file.extension == "gif") {
            Files.copy(file.toPath(), File(SnipSniper.imgFolder, file.name).toPath(), StandardCopyOption.REPLACE_EXISTING)
        } else {
            ImageIO.read(file).scaledSmooth(16, 16).also {
                ImageIO.write(it, file.extension, File(SnipSniper.imgFolder, file.name))
            }
        }
    }

    override fun close() = dispose()
}