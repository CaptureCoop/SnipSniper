package org.snipsniper.systray;

import org.snipsniper.ImageManager;
import org.snipsniper.LangManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.scviewer.SCViewerWindow;
import org.snipsniper.config.Config;
import org.snipsniper.utils.AboutWindow;
import org.snipsniper.utils.FileUtils;
import org.snipsniper.utils.Utils;
import org.snipsniper.utils.debug.LangDebugWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class Popup extends JFrame{
    private final static int TASKBAR_HEIGHT = 40;

    ArrayList<PopupMenu> menus = new ArrayList<>();

    public Popup(Sniper sniper) {
        Config config = sniper.getConfig();
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
        setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
        BufferedImage splash = ImageManager.getImage("splash.png");
        JLabel title = new JLabel(new ImageIcon(splash.getScaledInstance((int)(splash.getWidth()/3F),(int)(splash.getHeight()/3F), Image.SCALE_DEFAULT)));
        title.setText(sniper.getTitle());
        title.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        title.setVerticalTextPosition(JLabel.BOTTOM);
        title.setHorizontalTextPosition(JLabel.CENTER);
        add(title);
        add(new PopupMenuButton("Viewer", ImageManager.getImage("icons/viewer.png"), this, args -> new SCViewerWindow(null, config, false), menus));
        add(new PopupMenuButton("Editor", ImageManager.getImage("icons/editor.png"), this, args -> new SCEditorWindow(null, -1, -1, "SnipSniper Editor", config, true, null, false, false), menus));
        add(new JSeparator());
        add(new PopupMenuButton(LangManager.getItem("menu_open_image_folder"), ImageManager.getImage("icons/folder.png"), this, args -> {
            String folderToOpen = sniper.getConfig().getString(ConfigHelper.PROFILE.lastSaveFolder);
            if(folderToOpen.isEmpty() || folderToOpen.equalsIgnoreCase("none") || !new File(folderToOpen).exists()) {
                folderToOpen = sniper.getConfig().getString(ConfigHelper.PROFILE.pictureFolder);
            }
            FileUtils.openFolder(folderToOpen);
        }, menus));
        add(new PopupMenuButton(LangManager.getItem("menu_config"), ImageManager.getImage("icons/config.png"), this, args -> sniper.openConfigWindow(), menus));

        if (SnipSniper.isDebug()) {
            PopupMenu debugMenu = new PopupMenu("Debug", ImageManager.getImage("icons/random/kiwi.png"));
            debugMenu.add(new PopupMenuButton("Console", ImageManager.getImage("icons/console.png"), this, args -> SnipSniper.openDebugConsole(), menus));
            debugMenu.add(new PopupMenuButton("Language test", ImageManager.getImage("icons/config.png"), this, args -> new LangDebugWindow(), menus));
            add(debugMenu);
            menus.add(debugMenu);
        }

        add(new PopupMenuButton(LangManager.getItem("menu_about"), ImageManager.getImage("icons/about.png"), this, args -> new AboutWindow(sniper), menus));
        add(new JSeparator());
        add(new PopupMenuButton(LangManager.getItem("menu_quit"), ImageManager.getImage("icons/redx.png"), this, args -> SnipSniper.exit(false), menus));

        setIconImage(ImageManager.getImage("icons/snipsniper.png"));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                super.focusLost(focusEvent);
                setVisible(false);
            }
        });
    }

    public void showPopup(int x, int y) {
            setVisible(true);
            pack();

            //We do this in order to know which monitor the mouse position is on, before actually placing the popup jframe
            JFrame testGC = new JFrame();
            testGC.setUndecorated(true);
            testGC.setLocation(x, y);
            testGC.setVisible(true);
            GraphicsConfiguration gc = testGC.getGraphicsConfiguration();
            testGC.dispose();

            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            Rectangle screenRect = gc.getBounds();

            if(screenRect.x != 0 || screenRect.y != 0) {
                //This currently only allows non-default screens to work if taskbar is on bottom. Find better way!!
                //TODO: ^^^^^^^^^^^^^^^^^^
                //IDEA: Take half of the screens width to determine if we are left right bottom or top and then calculate position based on that, if possible
                setLocation(getX(), getY() - getHeight() - insets.bottom);
                if(!Utils.containsRectangleFully(screenRect, getBounds())) {
                    //Fallback
                    //TODO: Find prettier way
                    setLocation((int)screenRect.getWidth() / 2 - getWidth() / 2, (int)screenRect.getHeight() / 2 - getHeight() / 2);
                }
            } else {
                if (insets.bottom != 0)
                    setLocation(x, screenRect.height - getHeight() - insets.bottom);
                else if (insets.top != 0)
                    setLocation(x, insets.top);
                else if (insets.left != 0)
                    setLocation(insets.left, y - getHeight());
                else if (insets.right != 0)
                    setLocation(screenRect.width - getWidth() - insets.right, y - getHeight());
                else
                    setLocation(x, screenRect.height - getHeight() - TASKBAR_HEIGHT);
                /* If "Let taskbar scroll down when not in use" is enabled insets is all 0, use 40 for now, should work fine */
            }
            requestFocus();

    }
}
