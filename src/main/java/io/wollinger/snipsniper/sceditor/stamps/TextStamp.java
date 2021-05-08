package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class TextStamp implements IStamp{

    private PBRColor color;
    private int fontSize;
    private int fontSizeSpeed;
    private String text;

    private final ArrayList<Integer> nonTypeKeys = new ArrayList<>();

    private int fontMode = Font.PLAIN;
    private final Config config;

    public TextStamp(Config config) {
        this.config = config;

        nonTypeKeys.add(KeyEvent.VK_SHIFT);
        nonTypeKeys.add(KeyEvent.VK_CONTROL);
        nonTypeKeys.add(KeyEvent.VK_ALT);
        reset();
    }

    @Override
    public void update(InputContainer input, int mouseWheelDirection, KeyEvent keyEvent) {
        if(mouseWheelDirection == 1)
            fontSize -= fontSizeSpeed;
        else if(mouseWheelDirection == -1)
            fontSize += fontSizeSpeed;

        if(input.areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_B)) {
            fontMode++;
            if(fontMode > 2)
                fontMode = 0;
        }

        if(keyEvent != null) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (text.length() > 0)
                    text = text.substring(0, text.length() - 1);
                return;
            }

            if(!nonTypeKeys.contains(keyEvent.getKeyCode()) && !input.isKeyPressed(KeyEvent.VK_CONTROL))
                text += keyEvent.getKeyChar();
        }
    }

    @Override
    public void render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor, int historyPoint) {
        String textToDraw = "Text";
        if(!text.isEmpty())
            textToDraw = text;

        Font oldFont = g.getFont();
        Color oldColor = g.getColor();
        g.setFont(new Font("Arial", fontMode, fontSize));
        g.setColor(color.getColor());
        g.drawString(textToDraw, input.getMouseX(), input.getMouseY());
        g.setFont(oldFont);
        g.setColor(oldColor);

        if(isSaveRender)
            text = "";
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void reset() {
        text = "";
        color = new PBRColor(config.getColor("editorStampTextDefaultColor"));
        fontSize = config.getInt("editorStampTextDefaultFontSize");
        fontSizeSpeed = config.getInt("editorStampTextDefaultSpeed");
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public String getID() {
        return "editorStampText";
    }

    @Override
    public void setColor(PBRColor color) {
        this.color = color;
    }

    @Override
    public PBRColor getColor() {
        return color;
    }
}
