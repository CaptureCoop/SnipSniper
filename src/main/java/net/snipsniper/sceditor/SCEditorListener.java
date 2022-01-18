package net.snipsniper.sceditor;

import org.capturecoop.cclogger.CCLogger;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.sceditor.stamps.TextStamp;
import net.snipsniper.utils.ImageUtils;
import net.snipsniper.utils.InputContainer;
import net.snipsniper.utils.Utils;
import net.snipsniper.colorchooser.ColorChooser;
import net.snipsniper.sceditor.stamps.IStamp;
import net.snipsniper.snipscope.SnipScopeListener;
import org.capturecoop.cclogger.LogLevel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SCEditorListener extends SnipScopeListener {
    private final SCEditorWindow scEditorWindow;
    private final InputContainer input;
    private final ArrayList<BufferedImage> history = new ArrayList<>();
    private boolean openColorChooser = false;
    private boolean openSaveAsWindow = false;
    private boolean openNewImageWindow = false;

    public SCEditorListener(SCEditorWindow snipScopeWindow) {
        super(snipScopeWindow);
        scEditorWindow = snipScopeWindow;
        input = scEditorWindow.getInputContainer();
    }

    public void resetHistory() {
        LogManager.log("Reset editor history", LogLevel.INFO);
        history.clear();
        history.add(ImageUtils.copyImage(scEditorWindow.getImage()));
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);
        keyEvent.consume();
        //Hack for CTRL + N to work before isEnableInteraction is true
        //This means that even just pressing n allows you to create a new image
        //But thats not really bad, since N is not used for anything else in this context before
        //actually loading an image
        if(!scEditorWindow.isEnableInteraction() && keyEvent.getKeyCode() == KeyEvent.VK_N)
            openNewImageWindow = true;

        if(scEditorWindow.getInputContainer().areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_V)) {
            scEditorWindow.setSaveLocation("");
            scEditorWindow.setInClipboard(true);
            scEditorWindow.refreshTitle();
            scEditorWindow.setImage(ImageUtils.imageToBufferedImage(ImageUtils.getImageFromClipboard()), true, true);
        }

        if(!scEditorWindow.isEnableInteraction()) return;

        if(input.isKeyPressed(KeyEvent.VK_PERIOD))
            scEditorWindow.setEzMode(!scEditorWindow.isEzMode());

        TextStamp.TextState textState = TextStamp.TextState.TYPING;
        for(IStamp stamp : scEditorWindow.getStamps())
            if(stamp instanceof TextStamp)
                textState = ((TextStamp) stamp).getState();

        if(input.isKeyPressed(KeyEvent.VK_C) && textState == TextStamp.TextState.IDLE)
            openColorChooser = true;

        if(input.isKeyPressed(KeyEvent.VK_ENTER))
            openSaveAsWindow = true;

        if(scEditorWindow.getInputContainer().areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_N))
            openNewImageWindow = true;

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_1: scEditorWindow.setSelectedStamp(0); break;
            case KeyEvent.VK_2: scEditorWindow.setSelectedStamp(1); break;
            case KeyEvent.VK_3: scEditorWindow.setSelectedStamp(2); break;
            case KeyEvent.VK_4: scEditorWindow.setSelectedStamp(3); break;
            case KeyEvent.VK_5: scEditorWindow.setSelectedStamp(4); break;
            case KeyEvent.VK_6: scEditorWindow.setSelectedStamp(5); break;
            case KeyEvent.VK_7: scEditorWindow.setSelectedStamp(6); break;
        }

        if(scEditorWindow.getInputContainer().areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_S)) {
            if(scEditorWindow.isDirty)
                scEditorWindow.saveImage();
            scEditorWindow.close();
        }

        if(scEditorWindow.getInputContainer().areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_Z)) {
            int size = history.size();
            if(size > 1) {
                size--;
                history.remove(size);
                size--;
                scEditorWindow.setImage(ImageUtils.copyImage(history.get(size)), false, false);
                for(IStamp cStamp : scEditorWindow.getStamps())
                    cStamp.editorUndo(history.size());
            }
        }

        scEditorWindow.getSelectedStamp().update(scEditorWindow.getInputContainer(), 0, keyEvent);
        scEditorWindow.repaint();
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        super.keyReleased(keyEvent);
        if(openNewImageWindow) {
            //We do this here since creating a new image should not be blocked just because
            //the default image is still in there
            scEditorWindow.openNewImageWindow();
            openNewImageWindow = false;
            scEditorWindow.getInputContainer().resetKeys();
        }

        if(!scEditorWindow.isEnableInteraction()) return;

        if(openColorChooser) {
            //This fixes an issue with the ALT key getting "stuck" since the key up event is not being received if the color window is in the front.
            openColorChooser = false;
            scEditorWindow.getInputContainer().resetKeys();
            int x = (int)((scEditorWindow.getLocation().getX() + scEditorWindow.getWidth()/2));
            int y = (int)((scEditorWindow.getLocation().getY() + scEditorWindow.getHeight()/2));
            scEditorWindow.addClosableWindow(new ColorChooser(scEditorWindow.getConfig(), "Marker Color", scEditorWindow.getSelectedStamp().getColor(), scEditorWindow.getSelectedStamp().getID() + "DefaultColor", x, y, true, null));
        }

        if(openSaveAsWindow) {
            openSaveAsWindow = false;
            scEditorWindow.getInputContainer().resetKeys();
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(Utils.constructFilename(scEditorWindow.getConfig().getString(ConfigHelper.PROFILE.saveFormat), SCEditorWindow.FILENAME_MODIFIER)));
            if(chooser.showSaveDialog(chooser) == JFileChooser.APPROVE_OPTION){
                try {
                    if(chooser.getSelectedFile().createNewFile())
                        ImageIO.write(scEditorWindow.getImage(), "png", chooser.getSelectedFile());
                } catch (IOException ioException) {
                    LogManager.log("Error with loading image chosen for editor!", LogLevel.ERROR);
                    LogManager.logStacktrace(ioException, LogLevel.ERROR);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        super.mousePressed(mouseEvent);
        if(!scEditorWindow.isEnableInteraction()) return;

        if(!scEditorWindow.isPointOnUiComponents(mouseEvent.getPoint()))
            scEditorWindow.getSelectedStamp().mousePressedEvent(mouseEvent.getButton(), true);

        if(mouseEvent.getButton() == 3) {
            if(scEditorWindow.isDirty)
                scEditorWindow.saveImage();
            scEditorWindow.close();
        }

        scEditorWindow.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);

        if(scEditorWindow.isDefaultImage()) {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(scEditorWindow);
            if(option == JFileChooser.APPROVE_OPTION) {
                scEditorWindow.setImage(ImageUtils.imageToBufferedImage(new ImageIcon(fileChooser.getSelectedFile().getAbsolutePath()).getImage()), true, true);
            }
        }

        if(!scEditorWindow.isEnableInteraction()) return;

        if (!scEditorWindow.isPointOnUiComponents(mouseEvent.getPoint())) {
            scEditorWindow.getSelectedStamp().mousePressedEvent(mouseEvent.getButton(), false);

            scEditorWindow.getInputContainer().clearMousePath();
            if (!scEditorWindow.getInputContainer().isKeyPressed(scEditorWindow.getMovementKey())) {
                switch (mouseEvent.getButton()) {
                    case 1:
                        save(scEditorWindow.getImage().getGraphics(), false);
                        break;
                    case 2:
                        save(scEditorWindow.getImage().getGraphics(), true);
                        break;
                }
            }
        }

        scEditorWindow.repaint();
        scEditorWindow.requestFocus();
    }

    public void save(Graphics g, boolean isCensor) {
        scEditorWindow.isDirty = true;
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHints(scEditorWindow.getQualityHints());
        scEditorWindow.getSelectedStamp().render(g2, scEditorWindow.getInputContainer(), scEditorWindow.getPointOnImage(new Point(input.getMouseX(), input.getMouseY())), scEditorWindow.getDifferenceFromImage(), true, isCensor, history.size());
        scEditorWindow.repaint();
        g2.dispose();
        g.dispose();
        history.add(ImageUtils.copyImage(scEditorWindow.getImage()));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        super.mouseWheelMoved(mouseWheelEvent);
        if(!scEditorWindow.isEnableInteraction()) return;

        InputContainer input = scEditorWindow.getInputContainer();

        if(input.isKeyPressed(KeyEvent.VK_ALT)) {
            IStamp stamp = scEditorWindow.getSelectedStamp();
            Color oldColor = stamp.getColor().getPrimaryColor();
            final int alpha = stamp.getColor().getPrimaryColor().getAlpha();
            float[] hsv = new float[3];
            Color.RGBtoHSB(oldColor.getRed(),oldColor.getGreen(),oldColor.getBlue(),hsv);

            float speed = scEditorWindow.getConfig().getFloat(ConfigHelper.PROFILE.hsvColorSwitchSpeed) / 2500;
            if(mouseWheelEvent.getWheelRotation() == 1)
                hsv[0] += speed;
            else if(mouseWheelEvent.getWheelRotation() == -1)
                hsv[0] -= speed;

            Color newColor = Color.getHSBColor(hsv[0], hsv[1], hsv[2]);
            stamp.getColor().setPrimaryColor(newColor, alpha);
            scEditorWindow.repaint();
            return;
        }

        if(!input.isKeyPressed(scEditorWindow.getMovementKey()))
            scEditorWindow.getSelectedStamp().update(input, mouseWheelEvent.getWheelRotation(), null);
        scEditorWindow.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        super.mouseMoved(mouseEvent);
        if(!scEditorWindow.isEnableInteraction()) return;

        scEditorWindow.getInputContainer().setMousePosition(mouseEvent.getX(), mouseEvent.getY());
        scEditorWindow.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);
        if(!scEditorWindow.isEnableInteraction()) return;

        scEditorWindow.getInputContainer().addMousePathPoint(mouseEvent.getPoint());

        scEditorWindow.getInputContainer().setMousePosition(mouseEvent.getX(), mouseEvent.getY());
        scEditorWindow.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        super.mouseEntered(mouseEvent);
        scEditorWindow.setStampVisible(true);
        scEditorWindow.repaint();
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        super.mouseExited(mouseEvent);
        scEditorWindow.setStampVisible(false);
        scEditorWindow.repaint();
    }
}
