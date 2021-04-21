package io.wollinger.snipsniper.utils;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.systray.Sniper;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class ColorChooser extends JFrame{
	private static final long serialVersionUID = 8590714455238968415L;

	private Sniper sniper;

	private JColorChooser jcc;
	
	private final PBRColor color;
	private final String configKey;

	ColorChooser instance;
	
	public ColorChooser(Sniper sniper, String _title, PBRColor _color, String configKey) {
	    this.sniper = sniper;
		color = _color;
		instance = this;
		this.setTitle(_title);
		this.configKey = configKey;
		init();
	}
	
	public void close() {
		color.c = jcc.getColor();
		this.dispose();
	}

	public void save() {
	    color.c = jcc.getColor();
        if(configKey != null) {
            Config cfg = sniper.cfg;
            cfg.set(configKey, Utils.rgb2hex(color.c));
            cfg.save();
        }
        this.dispose();
    }
	
	void init() {
		jcc = new JColorChooser();
		jcc.setColor(color.c);
        AbstractColorChooserPanel[] panels = jcc.getChooserPanels();
        jcc.setPreviewPanel(new JPanel());
        for (AbstractColorChooserPanel accp : panels) {
            if (!accp.getDisplayName().equals("RGB")) {
                jcc.removeChooserPanel(accp);
            }
        }
        JPanel mainmain = new JPanel();
        JPanel colorPanel = new JPanel();
        JPanel submitButtonPanel = new JPanel();
        JButton submit = new JButton("Okay");
        submit.addActionListener(listener -> instance.close());

        JButton save = new JButton("Save as default");
        save.addActionListener(listener -> instance.save());

        mainmain.setLayout(new BoxLayout(mainmain, BoxLayout.Y_AXIS));
        
        colorPanel.add(jcc);
        
        mainmain.add(colorPanel);
        mainmain.add(submitButtonPanel);
        
        this.add(mainmain);
        this.setResizable(false);
        this.setFocusable(true);
        
        this.setAlwaysOnTop(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        this.pack();
        if(configKey != null) {
            save.setPreferredSize(new Dimension(this.getWidth() / 4, 50));
            submitButtonPanel.add(save);
        }
        submit.setPreferredSize(new Dimension(this.getWidth()/2, 50));
        submitButtonPanel.add(submit);

        this.pack();
        int w = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
		int h = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
		this.setLocation((w/2) - this.getWidth()/2, (h/2) - this.getHeight()/2);
        this.setVisible(true);
	}
}
