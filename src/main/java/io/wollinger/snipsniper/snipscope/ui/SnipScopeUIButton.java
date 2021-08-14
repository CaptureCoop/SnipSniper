package io.wollinger.snipsniper.snipscope.ui;

import io.wollinger.snipsniper.utils.Function;
import io.wollinger.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SnipScopeUIButton extends SnipScopeUIComponent{
    private BufferedImage icon;
    private BufferedImage iconPressed;
    private BufferedImage iconHovering;

    private ArrayList<Function> onPress = new ArrayList<>();
    private boolean isHovering = false;
    private boolean isHeld = false;
    private boolean selected = false;
    private Vector2Int lastPosition = new Vector2Int();

    public SnipScopeUIButton(BufferedImage icon, BufferedImage iconHovering, BufferedImage iconPressed) {
        this.icon = icon;
        this.iconHovering = iconHovering;
        this.iconPressed = iconPressed;
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        isHovering = contains(mouseEvent.getPoint());
        lastPosition = new Vector2Int(mouseEvent.getPoint());
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        lastPosition = new Vector2Int(mouseEvent.getPoint());
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if(isHovering)
            isHeld = true;
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if(isHeld) {
            isHeld = false;
            if(contains(lastPosition.toPoint())) {
                for (Function function : onPress) {
                    function.run();
                }
            }
        }
    }

    public void setSelected(boolean bool) {
        selected = bool;
    }

    @Override
    public void render(Graphics2D g) {
        BufferedImage toRender = icon;

        if(isHeld || selected)
            toRender = iconPressed;
        else if(isHovering)
            toRender = iconHovering;

        g.drawImage(toRender, getPosition().getX(), getPosition().getY(), getWidth(), getHeight(), null);
    }

    public void addOnPress(Function function) {
        onPress.add(function);
    }
}
