package net.snipsniper.configwindow.iconwindow;

import net.snipsniper.ImageManager;
import org.capturecoop.cclogger.CCLogger;
import net.snipsniper.SnipSniper;
import net.snipsniper.utils.*;
import org.capturecoop.cclogger.CCLogLevel;
import org.capturecoop.ccutils.utils.CCStringUtils;
import org.capturecoop.ccutils.utils.CCIClosable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


public class IconWindow extends JFrame implements CCIClosable {
    private final IconWindow instance;
    private final IFunction onSelectIcon;

    enum ICON_TYPE {GENERAL, RANDOM, CUSTOM}

    public IconWindow(String title, JFrame parent, IFunction onSelectIcon) {
        instance = this;
        this.onSelectIcon = onSelectIcon;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(512, 256);
        setTitle(title);
        setIconImage(ImageManager.Companion.getImage("icons/folder.png"));
        setLocation(parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                close();
            }
        });
        addMouseListener(new MouseAdapter() {
            boolean listenForExit = false;
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                listenForExit = true;
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                if(listenForExit) {
                    Rectangle bounds = new Rectangle(0, 0, getBounds().width, getBounds().height);
                    if(!bounds.contains(mouseEvent.getPoint()))
                        dispose();
                }
            }
        });

        setResizable(false);
        setVisible(true);
        JTabbedPane pane = new JTabbedPane();
        pane.addTab("General", setupPanel(ICON_TYPE.GENERAL));
        pane.addTab("Random", setupPanel(ICON_TYPE.RANDOM));
        pane.addTab("Custom", setupPanel(ICON_TYPE.CUSTOM));
        add(pane);

        pack();
        setSize(getWidth(), 256);
    }

    public JScrollPane setupPanel(ICON_TYPE type) {
        JPanel content = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        populateButtons(content, type);
        return scrollPane;
    }

    public void populateButtons(JPanel content, ICON_TYPE type) {
        content.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        final int MAX_X = 4;
        ArrayList<SSFile> list = new ArrayList<>();
        for(String file : ImageManager.Companion.getFilenameList()) {
            if (type == ICON_TYPE.GENERAL && file.contains("icons") && !file.contains("icons/random/"))
                list.add(new SSFile(file, SSFile.LOCATION.JAR));
            if (type == ICON_TYPE.RANDOM && file.contains("icons/random/"))
                list.add(new SSFile(file, SSFile.LOCATION.JAR));
        }
        if(type == ICON_TYPE.CUSTOM) {
            for(File localFile : FileUtils.listFiles(SnipSniper.Companion.getMainFolder() + "/img/")) {
                list.add(new SSFile(localFile.getName(), SSFile.LOCATION.LOCAL));
            }
        }
        int size = getRootPane().getWidth() / 5;
        Dimension sizeDim = new Dimension(size, size);
        for (SSFile file : list) {
            IconButton button = new IconButton(file.getPathWithLocation(), file.getLocation());
            button.setOnSelect(args -> {
                onSelectIcon.run(button.getId());
                dispose();
            });

            button.setOnDelete(args -> populateButtons(content, type));

            switch(file.getLocation()) {
                case JAR:
                    if(file.getPath().endsWith(".png"))
                        button.setIcon(new ImageIcon(ImageManager.Companion.getImage(file.getPath()).getScaledInstance(size, size, 0)));
                    else if(file.getPath().endsWith(".gif"))
                        button.setIcon(new ImageIcon(ImageManager.Companion.getAnimatedImage(file.getPath()).getScaledInstance(size, size, 0)));
                    break;
                case LOCAL:
                    button.setIcon(new ImageIcon(ImageUtils.getImageFromDisk(SnipSniper.Companion.getImgFolder() + "/" + file.getPath()).getScaledInstance(size, size, Image.SCALE_SMOOTH)));
                    break;
            }

            content.add(button, gbc);
            gbc.gridx++;
            if (gbc.gridx >= MAX_X)
                gbc.gridx = 0;
        }
        if(type == ICON_TYPE.CUSTOM) {
            content.setDropTarget(new DropTarget() {
                public synchronized void drop(DropTargetDropEvent evt) {
                    try {
                        evt.acceptDrop(DnDConstants.ACTION_COPY);
                        java.util.List droppedFiles = (List) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        for(Object fileObject : droppedFiles) {
                            File file = (File) fileObject;
                            loadFile(file);
                            populateButtons(content, type);
                        }
                    } catch (UnsupportedFlavorException | IOException e) {
                        CCLogger.log("Error setting up drop target for IconWindow", CCLogLevel.ERROR);
                        CCLogger.logStacktrace(e, CCLogLevel.ERROR);
                    }

                }
            });

            JButton customButton = new JButton("New");
            customButton.setPreferredSize(sizeDim);
            customButton.setMinimumSize(sizeDim);
            customButton.setMaximumSize(sizeDim);
            customButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                FileFilter fileFilter = new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if(f.isDirectory())
                            return true;
                        return CCStringUtils.endsWith(f.getName().toLowerCase(), ".png", ".gif", ".jpg", ".jpeg");
                    }

                    @Override
                    public String getDescription() {
                        return "Images";
                    }
                };
                fileChooser.addChoosableFileFilter(fileFilter);
                fileChooser.setFileFilter(fileFilter);
                int option = fileChooser.showOpenDialog(instance);
                if(option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    loadFile(file);
                    populateButtons(content, type);
                }
            });
            content.add(customButton, gbc);
        }
        content.revalidate();
        content.repaint();
    }

    public void loadFile(File file) {
        //We use Smooth Scaling for everything but gifs
        if(file.getName().endsWith("gif")) {
            try {
                Files.copy(file.toPath(), new File(SnipSniper.Companion.getImgFolder() + "/" + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ioException) {
                CCLogger.log("Could not load file for IconWindow! File: %c", CCLogLevel.ERROR, file.getAbsolutePath());
                CCLogger.logStacktrace(ioException, CCLogLevel.ERROR);
            }
        } else {
            try {
                BufferedImage img = ImageIO.read(file);
                img = ImageUtils.imageToBufferedImage(img.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                ImageIO.write(img, FileUtils.getFileExtension(file, false), new File(SnipSniper.Companion.getImgFolder() + "/" + file.getName()));
            } catch (IOException ioException) {
                CCLogger.log("Could not load file for IconWindow! File: %c", CCLogLevel.ERROR, file.getAbsolutePath());
                CCLogger.logStacktrace(ioException, CCLogLevel.ERROR);
            }
        }
    }

    @Override
    public void close() {
        dispose();
    }
}
