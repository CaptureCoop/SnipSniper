package org.snipsniper.sceditor.stamps;

import net.capturecoop.ccutils.math.Vector2Int;
import org.snipsniper.LogManager;
import org.snipsniper.config.Config;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.InputContainer;
import org.snipsniper.utils.SSColor;
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

    private final static String DEFAULT_TEXT = "Text";

    private final ArrayList<IStampUpdateListener> changeListeners = new ArrayList<>();

    private int lastDrawnWidth = 0;

    public TextStamp(Config config, SCEditorWindow scEditorWindow) {
        this.config = config;
        this.scEditorWindow = scEditorWindow;

        nonTypeKeys.add(KeyEvent.VK_SHIFT);
        nonTypeKeys.add(KeyEvent.VK_CONTROL);
        nonTypeKeys.add(KeyEvent.VK_ALT);
        //Arrow keys
        nonTypeKeys.add(KeyEvent.VK_LEFT);
        nonTypeKeys.add(KeyEvent.VK_RIGHT);
        nonTypeKeys.add(KeyEvent.VK_UP);
        nonTypeKeys.add(KeyEvent.VK_DOWN);
        reset();
    }

    @Override
    public void update(InputContainer input, int mouseWheelDirection, KeyEvent keyEvent) {
        switch(mouseWheelDirection) {
            case 1: fontSize -= fontSizeSpeed; break;
            case -1: fontSize += fontSizeSpeed; break;
        }

        if(input.areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_B)) {
            fontMode++;
            if(fontMode > 2)
                fontMode = 0;
        }

        if(scEditorWindow.isEzMode()) {
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
            return;
        }

        if(keyEvent != null && state == TextState.TYPING) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (text.length() > 0)
                    text = text.substring(0, text.length() - 1);
                alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
                return;
            }

            if(!nonTypeKeys.contains(keyEvent.getKeyCode()) && !input.isKeyPressed(KeyEvent.VK_CONTROL))
                text += keyEvent.getKeyChar();
        }
        alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
    }

    @Override
    public Rectangle render(Graphics g_, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        if(input == null) {
            input = new InputContainer();
            input.setMousePosition(position.getX(), position.getY());
        }

        Graphics2D g = (Graphics2D) g_;

        livePosition = new Vector2Int(input.getMouseX(), input.getMouseY()); //Update method only gets called upon keypress

        if(isSaveRender && !doSaveNextRender)
            return null;

        String textToDraw = getReadableText();
        int drawFontSize = (int) ((double)fontSize * difference[1]);

        Font oldFont = g.getFont();
        Paint oldColor = g.getPaint();
        g.setFont(new Font("Arial", fontMode, drawFontSize));
        int width = g.getFontMetrics().stringWidth(textToDraw);
        g.setPaint(color.getGradientPaint(width, drawFontSize, position.getX(), position.getY()));
        lastDrawnWidth = g.getFontMetrics().stringWidth(textToDraw);
        g.drawString(textToDraw, position.getX() - lastDrawnWidth / 2, position.getY());
        g.setFont(oldFont);
        g.setPaint(oldColor);
        
        if(isSaveRender) {
            state = TextState.IDLE;
            doSaveNextRender = false;
        }
        return new Rectangle(position.getX(), position.getY(), lastDrawnWidth, drawFontSize);
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void mousePressedEvent(int button, boolean pressed) {
        if(scEditorWindow.isEzMode()) {
            doSaveNextRender = true;
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
            return;
        }
        if(pressed && state == TextState.IDLE) {
            state = TextState.TYPING;
            cPosition = new Vector2Int(livePosition);
        } else if(pressed && state == TextState.TYPING) {
            doSaveNextRender = true;
        }
        alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
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

    public void setText(String text) {
        this.text = text;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    public String getReadableText() {
        if(text == null || text.isEmpty())
            return DEFAULT_TEXT;
        return text;
    }

    public String getText() {
        return text;
    }

    @Override
    public void setWidth(int width) {
        LogManager.log("setWidth called on TextStamp. This does not do anything! Use setHeight() instead.", LogLevel.WARNING);
    }

    @Override
    public int getWidth() {
        //Returns width in pixels
        return lastDrawnWidth;
    }

    @Override
    public void setHeight(int height) {
        fontSize = height;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    @Override
    public int getHeight() {
        return fontSize;
    }

    public void setFontMode(int fontMode) {
        this.fontMode = fontMode;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    public int getFontMode() {
        return fontMode;
    }

    @Override
    public String getID() {
        return "editorStampText";
    }

    @Override
    public void setColor(SSColor color) {
        this.color = color;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    @Override
    public SSColor getColor() {
        return color;
    }

    @Override
    public StampUtils.TYPE getType() {
        return StampUtils.TYPE.TEXT;
    }

    @Override
    public void addChangeListener(IStampUpdateListener listener) {
        changeListeners.add(listener);
    }

    public void alertChangeListeners(IStampUpdateListener.TYPE type) {
        for(IStampUpdateListener listener : changeListeners) {
            listener.updated(type);
        }
    }

    @Override
    public boolean doAlwaysRender() {
        return state != TextState.IDLE;
    }
}
