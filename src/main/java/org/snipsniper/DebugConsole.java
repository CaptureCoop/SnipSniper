package org.snipsniper;

import org.snipsniper.utils.CustomWindowListener;
import org.snipsniper.utils.Icons;
import org.snipsniper.utils.Links;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class DebugConsole extends JFrame {
    private final JTextPane content = new JTextPane();
    private final ArrayList<CustomWindowListener> listeners = new ArrayList<>();
    private int fontSize = 20;
    private final int scrollSpeed = 20;
    private final boolean[] keys = new boolean[4096];

    public DebugConsole () {
        setTitle("Debug Console");
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(size.getWidth()/2), (int)(size.getHeight()/2));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(Icons.getImage("icons/console.png"));
        content.setOpaque(true);
        content.setContentType("text/html");
        content.setEditable(false);
        content.setBackground(Color.BLACK);
        content.setFont(new Font("Consolas", Font.PLAIN, fontSize));

        JScrollPane scrollPane = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setWheelScrollingEnabled(false);
        add(scrollPane);

        content.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                keys[keyEvent.getKeyCode()] = true;
                if(keys[KeyEvent.VK_PLUS] || keys[KeyEvent.VK_ADD])
                    fontSize++;
                else if(keys[KeyEvent.VK_MINUS] || keys[KeyEvent.VK_SUBTRACT])
                    fontSize--;
                content.setFont(new Font(content.getFont().getName(), Font.PLAIN, fontSize));
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                keys[keyEvent.getKeyCode()] = false;
            }
        });

        addMouseWheelListener(e -> {
            if(keys[KeyEvent.VK_CONTROL]) {
                switch(e.getWheelRotation()) {
                    case -1: fontSize++; break;
                    case 1: fontSize--; break;
                }
            } else {
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getValue() + (e.getWheelRotation() * scrollSpeed));
            }
            content.setFont(new Font(content.getFont().getName(), Font.PLAIN, fontSize));
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for(CustomWindowListener listener : listeners)
                    listener.windowClosed();
                dispose();
            }
        });

        content.addHyperlinkListener(hle -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                Links.openLink(hle.getURL().toString());
            }
        });

        setVisible(true);
    }

    public void update() {
        if(LogManager.htmlLog != null) {
            content.setText("<html>" + LogManager.htmlLog + "</html>");
            repaint();
        }
    }

    public void addCustomWindowListener(CustomWindowListener listener) {
        listeners.add(listener);
    }

}
