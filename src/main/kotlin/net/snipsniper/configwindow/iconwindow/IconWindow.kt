package net.snipsniper.configwindow.iconwindow

import net.snipsniper.ImageManager
import net.snipsniper.SnipSniper
import net.snipsniper.utils.*
import org.capturecoop.ccutils.utils.CCIClosable
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Rectangle
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.awt.event.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileFilter

class IconWindow(title: String, parent: JFrame, private val onSelectIcon: IFunction): JFrame(), CCIClosable {
    private enum class IconType {GENERAL, RANDOM, CUSTOM}
    private val allowedExtensions = listOf(".png", ".gif", ".jpg", ".jpeg")
    private val rows = 4

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        size = Dimension(512, 256)
        iconImage = "icons/folder.png".getImage()
        this.title = title
        setLocation(parent.location.x + parent.width / 2 - width / 2, parent.location.y + parent.height / 2 - height / 2)
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(windowEvent: WindowEvent) = close()
        })

        isVisible = true
        JTabbedPane().also {
            fun s(t: String, tt: IconType) = it.addTab(t, setupPanel(tt))
            s("General", IconType.GENERAL)
            s("Random", IconType.RANDOM)
            s("Custom", IconType.CUSTOM)
            add(it)
        }
        setSize(width + 64, 320)
    }

    private fun setupPanel(type: IconType): JScrollPane {
        JPanel(GridBagLayout()).also { content ->
            populateButtons(content, type)
            return JScrollPane(content).also { scrollPane ->
                scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
                scrollPane.border = BorderFactory.createEmptyBorder()
                scrollPane.verticalScrollBar.unitIncrement = 20
            }
        }
    }

    private fun populateButtons(content: JPanel, type: IconType) {
        content.removeAll()
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.gridx = 0
        val list = ArrayList<SSFile>()
        ImageManager.filenameList.forEach { file ->
            if (type == IconType.GENERAL && file.contains("icons") && !file.contains("icons/random/"))
                list.add(SSFile(file, SSFile.LOCATION.JAR))
            if (type == IconType.RANDOM && file.contains("icons/random/"))
                list.add(SSFile(file, SSFile.LOCATION.JAR))
        }
        if (type == IconType.CUSTOM) {
            File(SnipSniper.imgFolder).walk().filter { it.isFile }.forEach { localFile ->
                list.add(SSFile(localFile.name, SSFile.LOCATION.LOCAL))
            }
        }
        val size = rootPane.width / 5
        val sizeDim = Dimension(size, size)
        list.forEach { file ->
            IconButton(file.getPathWithLocation(), file.location).also { btn ->
                btn.onSelect =  {
                    onSelectIcon.run(btn.id)
                    dispose()
                }
                btn.onDelete = { populateButtons(content, type) }
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
        if (type == IconType.CUSTOM) {
            content.dropTarget = object : DropTarget() {
                override fun drop(evt: DropTargetDropEvent) {
                    evt.acceptDrop(DnDConstants.ACTION_COPY)
                    (evt.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>).forEach {
                        it as File
                        loadFile(it)
                        populateButtons(content, type)
                    }
                }
            }
            JButton("New").also { newBtn ->
                newBtn.preferredSize = sizeDim
                newBtn.minimumSize = sizeDim
                newBtn.maximumSize = sizeDim
                newBtn.addActionListener {
                    JFileChooser().also { chooser ->
                        object : FileFilter() {
                            override fun accept(pathname: File) = if(pathname.isDirectory) true else allowedExtensions.any { pathname.name.lowercase().endsWith(it) }
                            override fun getDescription() = "Images"
                        }.also { filter ->
                            chooser.addChoosableFileFilter(filter)
                            chooser.fileFilter = filter
                        }
                        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                            loadFile(chooser.selectedFile)
                            populateButtons(content, type)
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