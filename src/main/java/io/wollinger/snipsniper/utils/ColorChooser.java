package io.wollinger.snipsniper.utils;

import io.wollinger.snipsniper.Config;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class ColorChooser extends JFrame{
	private static final long serialVersionUID = 8590714455238968415L;
	
    ColorChooser instance;
    private JColorChooser jcc;
	private final PBRColor color;
	private final String configKey;
    private final Config config;

	public ColorChooser(Config config, String title, PBRColor color, String configKey, int x, int y) {
        instance = this;
        this.config = config;
		this.color = color;
		this.configKey = configKey;

        setTitle(title);
        setIconImage(Icons.icon_taskbar);
		setAlwaysOnTop(true);
		init(x, y);
	}
	
	public void close() {
		color.setColor(jcc.getColor());
		dispose();
	}

	public void save() {
	    color.setColor(jcc.getColor());
        if(configKey != null) {
            config.set(configKey, Utils.rgb2hex(color.getColor()));
            config.save();
        }
        dispose();
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
        submit.addActionListener(listener -> instance.close());

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
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
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
}
