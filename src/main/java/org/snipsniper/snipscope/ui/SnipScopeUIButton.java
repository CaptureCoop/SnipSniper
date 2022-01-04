package org.snipsniper.snipscope.ui;

import org.snipsniper.utils.IFunction;
import net.capturecoop.ccmathutils.Vector2Int;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SnipScopeUIButton extends SnipScopeUIComponent{
    private final BufferedImage icon;
    private final BufferedImage iconPressed;
    private final BufferedImage iconHovering;

    private final ArrayList<IFunction> onPress = new ArrayList<>();
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
    public boolean mouseMoved(MouseEvent mouseEvent) {
        if(!super.mouseMoved(mouseEvent)) return false;
        isHovering = contains(mouseEvent.getPoint());
        lastPosition = new Vector2Int(mouseEvent.getPoint());
        return true;
    }

    @Override
    public boolean mouseDragged(MouseEvent mouseEvent) {
        if(!super.mouseDragged(mouseEvent)) return false;
        lastPosition = new Vector2Int(mouseEvent.getPoint());
        return true;
    }

    @Override
    public boolean mousePressed(MouseEvent mouseEvent) {
        if(!super.mousePressed(mouseEvent)) return false;
        if(isHovering)
            isHeld = true;
        return true;
    }

    @Override
    public boolean mouseReleased(MouseEvent mouseEvent) {
        if(!super.mouseReleased(mouseEvent)) return false;
        if(isHeld) {
            isHeld = false;
            if(contains(lastPosition.toPoint())) {
                for (IFunction function : onPress) {
                    function.run();
                }
            }
        }
        return true;
    }

    @Override
    public boolean render(Graphics2D g) {
        if(!super.render(g)) return false;
        BufferedImage toRender = icon;

        if(isHeld || selected)
            toRender = iconPressed;
        else if(isHovering)
            toRender = iconHovering;

        g.drawImage(toRender, getPosition().getX(), getPosition().getY(), getWidth(), getHeight(), null);
        return true;
    }

    public void setSelected(boolean bool) {
        selected = bool;
    }

    public void addOnPress(IFunction function) {
        onPress.add(function);
    }
}
