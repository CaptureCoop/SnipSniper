package org.snipsniper.colorchooser;

import org.snipsniper.ImageManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.config.Config;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.*;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class ColorChooser extends JFrame implements IClosable {
    private final ColorChooser instance;
    private JColorChooser jcc;
    private final SSColor colorToChange;
    private final SSColor color;
	private final String configKey;
    private final Config config;
    private final ArrayList<CustomWindowListener> listeners = new ArrayList<>();

	public ColorChooser(Config config, String title, SSColor color, String configKey, int x, int y, boolean useGradient, BufferedImage previewBackground) {
        instance = this;
        this.config = config;
        colorToChange = color;
		this.color = new SSColor(color);
		this.configKey = configKey;

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close(false);
            }
        });

        setTitle(title);
        setIconImage(ImageManager.getImage("icons/snipsniper.png"));
		init(x, y, useGradient, previewBackground);
	}
	
	public void close(boolean doSave) {
		if(doSave)
            colorToChange.loadFromSSColor(color);

	    for(CustomWindowListener listener : listeners)
		    listener.windowClosed();

		dispose();
	}

	public void save() {
        if(configKey != null) {
            config.set(configKey, color.toSaveString());
            config.save();
        }
        close(true);
    }
	
	void init(int x, int y, boolean useGradient, BufferedImage previewBackground) {
		jcc = new JColorChooser();
		jcc.setColor(color.getPrimaryColor());
        AbstractColorChooserPanel[] panels = jcc.getChooserPanels();
        jcc.setPreviewPanel(new JPanel());
        for (AbstractColorChooserPanel colorPanel : panels) {
            if (!colorPanel.getDisplayName().equalsIgnoreCase(SnipSniper.getConfig().getString(ConfigHelper.MAIN.colorChooserType))) {
                jcc.removeChooserPanel(colorPanel);
            }
        }
        JPanel mainPanel = new JPanel();
        JPanel colorPanel = new JPanel();
        JPanel submitButtonPanel = new JPanel();
        JButton submit = new JButton("Okay");
        submit.addActionListener(e -> instance.close(true));

        JButton save = new JButton("Save as default");
        save.addActionListener(listener -> instance.save());

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        colorPanel.add(jcc);

        mainPanel.add(new ColorChooserPreviewPanel(this, useGradient, previewBackground));
        mainPanel.add(colorPanel);
        mainPanel.add(submitButtonPanel);
        
        add(mainPanel);
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

	public JColorChooser getJcc() {
	    return jcc;
    }

	public SSColor getColor() {
	    return color;
    }

	public void addWindowListener(CustomWindowListener listener) {
	    listeners.add(listener);
    }

    @Override
    public void close() {
        dispose();
    }
}
