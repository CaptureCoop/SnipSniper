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
    private boolean isPressed = false;
    private Vector2Int lastPosition = new Vector2Int();

    public SnipScopeUIButton(BufferedImage icon, BufferedImage iconHovering, BufferedImage iconPressed) {
        this.icon = icon;
        this.iconHovering = iconHovering;
        this.iconPressed = iconPressed;
    }

    public SnipScopeUIButton(BufferedImage icon, BufferedImage iconHovering, BufferedImage iconPressed, Function onPress) {
        this.icon = icon;
        this.iconHovering = iconHovering;
        this.iconPressed = iconPressed;
        this.onPress.add(onPress);
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
            isPressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if(isPressed) {
            isPressed = false;
            if(contains(lastPosition.toPoint())) {
                for (Function function : onPress) {
                    function.run();
                }
            }
        }
    }


    @Override
    public void render(Graphics2D g) {
        BufferedImage toRender = icon;

        if(isPressed)
            toRender = iconPressed;
        else if(isHovering)
            toRender = iconHovering;

        g.drawImage(toRender, getPosition().getX(), getPosition().getY(), getWidth(), getHeight(), null);
    }

    public void addOnPress(Function function) {
        onPress.add(function);
    }
}
