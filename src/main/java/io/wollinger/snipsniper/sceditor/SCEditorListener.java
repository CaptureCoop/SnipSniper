package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.sceditor.stamps.IStamp;
import io.wollinger.snipsniper.snipscope.SnipScopeListener;
import io.wollinger.snipsniper.utils.*;

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
import java.util.logging.Level;

public class SCEditorListener extends SnipScopeListener {
    private final SCEditorWindow scEditorWindow;
    private final InputContainer input;
    private final ArrayList<BufferedImage> history = new ArrayList<>();
    private boolean openColorChooser = false;
    private boolean openSaveAsWindow = false;

    public SCEditorListener(SCEditorWindow snipScopeWindow) {
        super(snipScopeWindow);
        scEditorWindow = snipScopeWindow;
        input = scEditorWindow.getInputContainer();
    }

    public void resetHistory() {
        LogManager.log(scEditorWindow.getID(), "Reset editor history", Level.INFO);
        history.clear();
        history.add(Utils.copyImage(scEditorWindow.getImage()));
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);
        keyEvent.consume();

        if(input.isKeyPressed(KeyEvent.VK_C))
            openColorChooser = true;

        if(input.isKeyPressed(KeyEvent.VK_ENTER))
            openSaveAsWindow = true;

        if(scEditorWindow.getInputContainer().areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_V)) {
            scEditorWindow.setSaveLocation("");
            scEditorWindow.setInClipboard(true);
            scEditorWindow.refreshTitle();
            scEditorWindow.setImage(Utils.imageToBufferedImage(Utils.getImageFromClipboard()), true, true);
        }

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_1: scEditorWindow.setSelectedStamp(0); break;
            case KeyEvent.VK_2: scEditorWindow.setSelectedStamp(1); break;
            case KeyEvent.VK_3: scEditorWindow.setSelectedStamp(2); break;
            case KeyEvent.VK_4: scEditorWindow.setSelectedStamp(3); break;
            case KeyEvent.VK_5: scEditorWindow.setSelectedStamp(4); break;
            case KeyEvent.VK_6: scEditorWindow.setSelectedStamp(5); break;
        }

        if(scEditorWindow.getInputContainer().areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_S)) {
            if(scEditorWindow.isDirty)
                scEditorWindow.saveImage();
            scEditorWindow.dispose();
        }

        if(scEditorWindow.getInputContainer().areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_Z)) {
            int size = history.size();
            if(size > 1) {
                size--;
                history.remove(size);
                size--;
                scEditorWindow.setImage(Utils.copyImage(history.get(size)), false, false);
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

        if(openColorChooser) {
            //This fixes an issue with the ALT key getting "stuck" since the key up event is not being received if the color window is in the front.
            openColorChooser = false;
            int x = (int)((scEditorWindow.getLocation().getX() + scEditorWindow.getWidth()/2));
            int y = (int)((scEditorWindow.getLocation().getY() + scEditorWindow.getHeight()/2));
            new ColorChooser(scEditorWindow.getConfig(), "Marker Color", scEditorWindow.getSelectedStamp().getColor(), scEditorWindow.getSelectedStamp().getID() + "DefaultColor", x, y);
        }

        if(openSaveAsWindow) {
            openSaveAsWindow = false;
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(Utils.constructFilename(SCEditorWindow.FILENAME_MODIFIER)));
            if(chooser.showSaveDialog(chooser) == JFileChooser.APPROVE_OPTION){
                try {
                    if(chooser.getSelectedFile().createNewFile())
                        ImageIO.write(scEditorWindow.getImage(), "png", chooser.getSelectedFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        super.mousePressed(mouseEvent);

        scEditorWindow.getSelectedStamp().mousePressedEvent(mouseEvent.getButton(), true);

        if(mouseEvent.getButton() == 3) {
            if(scEditorWindow.isDirty)
                scEditorWindow.saveImage();
            scEditorWindow.dispose();
        }

        scEditorWindow.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);

        scEditorWindow.getSelectedStamp().mousePressedEvent(mouseEvent.getButton(), false);

        scEditorWindow.getInputContainer().clearMousePath();
        if(!scEditorWindow.getInputContainer().isKeyPressed(scEditorWindow.getMovementKey())) {
            switch(mouseEvent.getButton()) {
                case 1: save(scEditorWindow.getImage().getGraphics(), false); break;
                case 2: save(scEditorWindow.getImage().getGraphics(), true); break;
            }
        }
        scEditorWindow.repaint();
    }

    public void save(Graphics g, boolean isCensor) {
        scEditorWindow.isDirty = true;
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHints(scEditorWindow.getQualityHints());
        scEditorWindow.getSelectedStamp().render(g2, scEditorWindow.getInputContainer(), scEditorWindow.getPointOnImage(new Point(input.getMouseX(), input.getMouseY())), scEditorWindow.getDifferenceFromImage(), true, isCensor, history.size());
        scEditorWindow.repaint();
        g2.dispose();
        g.dispose();
        history.add(Utils.copyImage(scEditorWindow.getImage()));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        super.mouseWheelMoved(mouseWheelEvent);

        InputContainer input = scEditorWindow.getInputContainer();

        if(input.isKeyPressed(KeyEvent.VK_ALT)) {
            IStamp stamp = scEditorWindow.getSelectedStamp();
            Color oldColor = stamp.getColor().getColor();
            final int alpha = stamp.getColor().getColor().getAlpha();
            float[] hsv = new float[3];
            Color.RGBtoHSB(oldColor.getRed(),oldColor.getGreen(),oldColor.getBlue(),hsv);

            float speed = scEditorWindow.getConfig().getFloat(ConfigHelper.PROFILE.hsvColorSwitchSpeed) / 2500;
            if(mouseWheelEvent.getWheelRotation() == 1)
                hsv[0] += speed;
            else if(mouseWheelEvent.getWheelRotation() == -1)
                hsv[0] -= speed;

            Color newColor = Color.getHSBColor(hsv[0], hsv[1], hsv[2]);
            stamp.setColor(new PBRColor(newColor, alpha));
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

        scEditorWindow.getInputContainer().setMousePosition(mouseEvent.getX(), mouseEvent.getY());
        scEditorWindow.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);

        scEditorWindow.getInputContainer().addMousePathPoint(mouseEvent.getPoint());

        scEditorWindow.getInputContainer().setMousePosition(mouseEvent.getX(), mouseEvent.getY());
        scEditorWindow.repaint();
    }
}
