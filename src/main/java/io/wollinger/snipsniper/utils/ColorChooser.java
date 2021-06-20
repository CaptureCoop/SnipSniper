package io.wollinger.snipsniper.utils;

import io.wollinger.snipsniper.Config;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class ColorChooser extends JFrame{
    private final ColorChooser instance;
    private JColorChooser jcc;
	private final PBRColor color;
	private final String configKey;
    private final Config config;

    private final ArrayList<CustomWindowListener> listeners = new ArrayList<>();

	public ColorChooser(Config config, String title, PBRColor color, String configKey, int x, int y) {
        instance = this;
        this.config = config;
		this.color = color;
		this.configKey = configKey;

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) { }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                close();
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

        setTitle(title);
        setIconImage(Icons.icon_taskbar);
		setAlwaysOnTop(true);
		init(x, y);
	}
	
	public void close() {
		for(CustomWindowListener listener : listeners) {
		    listener.windowClosed();
        }
		dispose();
	}

	public void save() {
        if(configKey != null) {
            config.set(configKey, Utils.rgb2hex(color.getColor()));
            config.save();
        }
        close();
    }
	
	void init(int x, int y) {
		jcc = new JColorChooser();
		jcc.setColor(color.getColor());
        AbstractColorChooserPanel[] panels = jcc.getChooserPanels();
        jcc.setPreviewPanel(new JPanel());
        for (AbstractColorChooserPanel colorPanel : panels) {
            if (!colorPanel.getDisplayName().equals("RGB")) {
                jcc.removeChooserPanel(colorPanel);
            }
        }
        JPanel mainPanel = new JPanel();
        JPanel colorPanel = new JPanel();
        JPanel submitButtonPanel = new JPanel();
        JButton submit = new JButton("Okay");
        submit.addActionListener(e -> {
            color.setColor(jcc.getColor());
            instance.close();
        });

        JButton save = new JButton("Save as default");
        save.addActionListener(listener -> instance.save());

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        colorPanel.add(jcc);

        mainPanel.add(colorPanel);
        mainPanel.add(submitButtonPanel);
        
        add(mainPanel);
        setResizable(false);
        setFocusable(true);
        
        setAlwaysOnTop(true);

        pack();
        if(configKey != null) {
            save.setPreferredSize(new Dimension(this.getWidth() / 4, 50));
            submitButtonPanel.add(save);
        }
        submit.setPreferredSize(new Dimension(this.getWidth()/2, 50));
        submitButtonPanel.add(submit);

        pack();
        setLocation(x - getWidth()/2, y - getHeight()/2);
        setVisible(true);
	}

	public void addWindowListener(CustomWindowListener listener) {
	    listeners.add(listener);
    }
}
