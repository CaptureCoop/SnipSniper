package org.snipsniper.sceditor.stamps;

import org.snipsniper.LogManager;
import org.snipsniper.config.Config;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.InputContainer;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Vector2Int;
import org.snipsniper.utils.enums.LogLevel;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class TextStamp implements IStamp{
    private final Config config;
    private final SCEditorWindow scEditorWindow;

    private SSColor color;
    private int fontSize;
    private int fontSizeSpeed;
    private String text;

    private final ArrayList<Integer> nonTypeKeys = new ArrayList<>();

    private int fontMode = Font.PLAIN;

    private TextState state = TextState.IDLE;
    private Vector2Int cPosition = new Vector2Int();
    private Vector2Int livePosition = new Vector2Int();
    private boolean doSaveNextRender = false;

    public enum TextState {IDLE, TYPING}

    public TextStamp(Config config, SCEditorWindow scEditorWindow) {
        this.config = config;
        this.scEditorWindow = scEditorWindow;

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

        if(keyEvent != null && state == TextState.TYPING) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (text.length() > 0)
                    text = text.substring(0, text.length() - 1);
                if(scEditorWindow != null)
                    scEditorWindow.updateEzUI();
                return;
            }

            if(!nonTypeKeys.contains(keyEvent.getKeyCode()) && !input.isKeyPressed(KeyEvent.VK_CONTROL))
                text += keyEvent.getKeyChar();
            if(scEditorWindow != null)
                scEditorWindow.updateEzUI();
        }
    }

    @Override
    public Rectangle render(Graphics g_, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        if(input == null) {
            input = new InputContainer();
            input.setMousePosition(position.getX(), position.getY());
        }

        Graphics2D g = (Graphics2D) g_;

        livePosition = new Vector2Int(input.getMouseX(), input.getMouseY()); //Update method only gets called upon keypress

        Point pointToUseForRenderPos = new Point(input.getMouseX(), input.getMouseY());

        if(state == TextState.TYPING)
            pointToUseForRenderPos = new Point(cPosition.toPoint());

        if(isSaveRender && !doSaveNextRender)
            return null;

        Vector2Int renderPos = position;
        if(scEditorWindow != null)
            renderPos = scEditorWindow.getPointOnImage(pointToUseForRenderPos);

        String textToDraw = "Text";
        if(!text.isEmpty())
            textToDraw = text;

        int drawFontSize = (int) ((double)fontSize * difference[1]);

        Font oldFont = g.getFont();
        Paint oldColor = g.getPaint();
        g.setFont(new Font("Arial", fontMode, drawFontSize));
        int width = g.getFontMetrics().stringWidth(textToDraw);
        g.setPaint(color.getGradientPaint(width, drawFontSize, renderPos.getX(), renderPos.getY()));
        g.drawString(textToDraw, renderPos.getX(), renderPos.getY());
        g.setFont(oldFont);
        g.setPaint(oldColor);
        
        if(isSaveRender) {
            reset();
        }
        return new Rectangle(renderPos.getX(), renderPos.getY(), g.getFontMetrics().stringWidth(textToDraw), drawFontSize);
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void mousePressedEvent(int button, boolean pressed) {
        if(pressed && state == TextState.IDLE) {
            state = TextState.TYPING;
            cPosition = new Vector2Int(livePosition);
        } else if(pressed && state == TextState.TYPING) {
            doSaveNextRender = true;
        }
    }

    public TextState getState() {
        return state;
    }

    @Override
    public void reset() {
        text = "";
        state = TextState.IDLE;
        doSaveNextRender = false;
        color = config.getColor(ConfigHelper.PROFILE.editorStampTextDefaultColor);
        fontSize = config.getInt(ConfigHelper.PROFILE.editorStampTextDefaultFontSize);
        fontSizeSpeed = config.getInt(ConfigHelper.PROFILE.editorStampTextDefaultSpeed);
    }

    @Override
    public void setWidth(int width) {
        LogManager.log("setWidth called on TextStamp. This does not do anything! Use setHeight() instead.", LogLevel.WARNING);
    }

    @Override
    public int getWidth() {
        //TODO: Return width in pixels?
        LogManager.log("getWidth called on TextStamp. This does not return anything! Use getHeight() instead.", LogLevel.WARNING);
        return 0;
    }

    @Override
    public void setHeight(int height) {
        fontSize = height;
    }

    @Override
    public int getHeight() {
        return fontSize;
    }

    @Override
    public String getID() {
        return "editorStampText";
    }

    @Override
    public void setColor(SSColor color) {
        this.color = color;
    }

    @Override
    public SSColor getColor() {
        return color;
    }

    @Override
    public StampUtils.TYPE getType() {
        return StampUtils.TYPE.TEXT;
    }
}
