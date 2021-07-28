package io.wollinger.snipsniper.colorchooser;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.CustomWindowListener;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.SSColor;
import io.wollinger.snipsniper.utils.Utils;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorChooser extends JFrame{
    private final ColorChooser instance;
    private JColorChooser jcc;
	private final SSColor color;
	private final String configKey;
    private final Config config;

    private final ArrayList<CustomWindowListener> listeners = new ArrayList<>();

	public ColorChooser(Config config, String title, SSColor color, String configKey, int x, int y, boolean useGradient) {
        instance = this;
        this.config = config;
		this.color = color;
		this.configKey = configKey;

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        setTitle(title);
        setIconImage(Icons.icon_taskbar);
		setAlwaysOnTop(true);
		init(x, y, useGradient);
	}
	
	public void close() {
		for(CustomWindowListener listener : listeners) {
		    listener.windowClosed();
        }
		dispose();
	}

	public void save() {
        if(configKey != null) {
            config.set(configKey, Utils.rgb2hex(color.getPrimaryColor()));
            config.save();
        }
        close();
    }
	
	void init(int x, int y, boolean useGradient) {
		jcc = new JColorChooser();
		jcc.setColor(color.getPrimaryColor());
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
        submit.addActionListener(e -> instance.close());

        JButton save = new JButton("Save as default");
        save.addActionListener(listener -> instance.save());

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        colorPanel.add(jcc);

        ColorChooserPreviewPanel gradientPanel = null;
        if(useGradient) {
            gradientPanel = new ColorChooserPreviewPanel(this);
        }

        if(gradientPanel != null)
            mainPanel.add(gradientPanel);
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

	public JColorChooser getJcc() {
	    return jcc;
    }

	public SSColor getColor() {
	    return color;
    }

	public void addWindowListener(CustomWindowListener listener) {
	    listeners.add(listener);
    }
}
