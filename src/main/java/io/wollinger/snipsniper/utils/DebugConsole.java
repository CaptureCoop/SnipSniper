package io.wollinger.snipsniper.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

public class DebugConsole extends JFrame {

    private JTextPane content = new JTextPane();
    private int fontSize = 20;
    private ArrayList<CustomWindowListener> listeners = new ArrayList<>();

    public DebugConsole () {
        setTitle("Debug Console");
        setSize(512, 512);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(Icons.icon_console);
        content.setOpaque(true);
        content.setContentType("text/html");
        content.setEditable(false);
        content.setBackground(Color.BLACK);
        content.setFont(new Font("Consolas", Font.PLAIN, fontSize));

        JScrollPane scrollPane = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        add(scrollPane);

        content.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) { }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.VK_PLUS || keyEvent.getKeyCode() == KeyEvent.VK_ADD)
                    fontSize++;
                else if(keyEvent.getKeyCode() == KeyEvent.VK_MINUS || keyEvent.getKeyCode() == KeyEvent.VK_SUBTRACT)
                    fontSize--;
                content.setFont(new Font(content.getFont().getName(), Font.PLAIN, fontSize));
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) { }
        });

        addMouseWheelListener(e -> {
            switch(e.getWheelRotation()) {
                case -1: fontSize++; break;
                case 1: fontSize--; break;
            }
            content.setFont(new Font(content.getFont().getName(), Font.PLAIN, fontSize));
        });

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) { }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                for(CustomWindowListener listener : listeners)
                    listener.windowClosed();
                dispose();
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) { }

            @Override
            public void windowIconified(WindowEvent windowEvent) { }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) { }

            @Override
            public void windowActivated(WindowEvent windowEvent) { }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) { }
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
