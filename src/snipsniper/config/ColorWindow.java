package snipsniper.config;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import snipsniper.Icons;
import snipsniper.Utils;

public class ColorWindow extends JFrame implements WindowListener{

	private static final long serialVersionUID = 8822010540160371736L;

	ConfigWindow cfgWnd;

	JPanel colorPreview;
	
	JLabel rValue = new JLabel("");
	JLabel gValue = new JLabel("");
	JLabel bValue = new JLabel("");
	
	ColorSlider rSlider;
	ColorSlider gSlider;
	ColorSlider bSlider;

	JTextField hex = new JTextField();
	
	ColorWindow instance = this;
	
	public ColorWindow(ConfigWindow _cfgWnd) {
		cfgWnd = _cfgWnd;
		this.setSize(512,512);
		this.setTitle("Color");
		this.setVisible(true);
		this.setIconImage(Icons.icon);
		
		rSlider = new ColorSlider(1,this);
		gSlider = new ColorSlider(2,this);
		bSlider = new ColorSlider(3,this);
		
		rValue.setText(cfgWnd.borderColor.getRed() + "");
		gValue.setText(cfgWnd.borderColor.getGreen() + "");
		bValue.setText(cfgWnd.borderColor.getBlue() + "");
		hex.setText(Utils.rgb2hex(cfgWnd.borderColor));
		
		this.addWindowListener(this);
		
		JPanel options = new JPanel(new GridLayout(0,1));
		
		JPanel row1 = new JPanel(new GridLayout(0,3));
		colorPreview = new JPanel();
		colorPreview.setBackground(cfgWnd.sniperInstance.cfg.getColor("borderColor"));
		colorPreview.setOpaque(true);
		row1.add(new JPanel());
		row1.add(colorPreview);
		options.add(row1);
		
		JPanel row2 = new JPanel(new GridLayout(0,3));
		row2.add(new JPanel());
		row2.add(rSlider);
		row2.add(rValue);
		options.add(row2);
		
		JPanel row3 = new JPanel(new GridLayout(0,3));
		row3.add(new JPanel());
		row3.add(gSlider);
		row3.add(gValue);
		options.add(row3);
		
		JPanel row4 = new JPanel(new GridLayout(0,3));
		row4.add(new JPanel());
		row4.add(bSlider);
		row4.add(bValue);
		options.add(row4);
		
		JPanel row5 = new JPanel(new GridLayout(0,3));
		JButton btnSave = new JButton("Okay");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				instance.save();
			}
		});
		row5.add(new JPanel());
		row5.add(btnSave);
		row5.add(hex);
		options.add(row5);
		
		hex.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateSliders();
			}
		});
		
		this.add(options);
		int w = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
		int h = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
		this.setLocation((w/2) - this.getWidth()/2, (h/2) - this.getHeight()/2);
		updateSliders();
	}
	
	void updateSliders() {
		Color newColor = Utils.hex2rgb(hex.getText());
		rSlider.setValue(newColor.getRed());
		gSlider.setValue(newColor.getGreen());
		bSlider.setValue(newColor.getBlue());
		colorPreview.setBackground(newColor);
	}
	
	void save() {
		cfgWnd.borderColor = colorPreview.getBackground();
		close();
	}

	private boolean closed = false;
	void close() {
		if(!closed) {
			closed = true;
			this.dispose();
			cfgWnd.cWnd = null;
		}
	}
	
	@Override
	public void windowActivated(WindowEvent e) { }

	@Override
	public void windowClosed(WindowEvent e) {
		close();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		close();
	}

	@Override
	public void windowDeactivated(WindowEvent e) { }

	@Override
	public void windowDeiconified(WindowEvent e) { }

	@Override
	public void windowIconified(WindowEvent e) { }

	@Override
	public void windowOpened(WindowEvent e) { }
	
}
