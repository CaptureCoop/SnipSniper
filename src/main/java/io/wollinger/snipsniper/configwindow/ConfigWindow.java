package io.wollinger.snipsniper.configwindow;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.*;
import org.jnativehook.GlobalScreen;

public class ConfigWindow extends JFrame implements WindowListener{
	private HotKeyButton hotKeyButton;
	private JCheckBox saveToDisk = new JCheckBox();
	private JCheckBox copyToClipboard = new JCheckBox();
	private JTextField borderSize = new JTextField();
	private JTextField pictureLocation = new JTextField();
	private PBRColor borderColor;
	private JTextField snipeDelay = new JTextField();
	private JCheckBox openEditor = new JCheckBox();

	private int maxBorder = 999;

	private ColorChooser colorChooser = null;
	private Sniper sniperInstance;
	
	public JLabel createJLabel(String title, int horizontalAlignment, int verticalAlignment) {
		JLabel jlabel = new JLabel(title);
		jlabel.setHorizontalAlignment(horizontalAlignment);
		jlabel.setVerticalAlignment(verticalAlignment);
		return jlabel;
	}
	
	public ConfigWindow(Sniper sniperInstance) {
		this.sniperInstance = sniperInstance;
		addWindowListener(this);
		setSize(512,512);
		setTitle(LangManager.getItem("config_label_config"));
		setIconImage(Icons.icon_taskbar);

		hotKeyButton = new HotKeyButton(sniperInstance.cfg.getString("hotkey"));
		saveToDisk.setSelected(sniperInstance.cfg.getBool("saveToDisk"));
		copyToClipboard.setSelected(sniperInstance.cfg.getBool("copyToClipboard"));
		borderSize.setText(sniperInstance.cfg.getInt("borderSize") + "");
		pictureLocation.setText(sniperInstance.cfg.getRawString("pictureFolder") + "");
		borderColor = new PBRColor(sniperInstance.cfg.getColor("borderColor"));
		snipeDelay.setText(sniperInstance.cfg.getInt("snipeDelay") + "");
		openEditor.setSelected(sniperInstance.cfg.getBool("openEditor"));
		
		JButton saveButton = new JButton(LangManager.getItem("config_label_save"));
		saveButton.addActionListener(e -> save());
		
		JPanel options = new JPanel(new GridLayout(0,1));

		JPanel row0 = new JPanel(new GridLayout(0,2));
		row0.add(createJLabel(LangManager.getItem("config_label_hotkey"), JLabel.CENTER, JLabel.CENTER));
		JPanel row0_1 = new JPanel(new GridLayout(0,2));
		row0_1.add(hotKeyButton);
		JButton deleteHotKey = new JButton(LangManager.getItem("config_label_delete"));
		deleteHotKey.addActionListener(e -> {
			hotKeyButton.setText(LangManager.getItem("config_label_none"));
			hotKeyButton.hotkey = -1;
		});
		row0_1.add(deleteHotKey);
		row0.add(row0_1);
		options.add(row0);
		
		JPanel row1 = new JPanel(new GridLayout(0,2));
		row1.add(createJLabel(LangManager.getItem("config_label_saveimages"), JLabel.CENTER, JLabel.CENTER));
		row1.add(saveToDisk);
		options.add(row1);
		
		JPanel row2 = new JPanel(new GridLayout(0,2));
		row2.add(createJLabel(LangManager.getItem("config_label_copyclipboard"), JLabel.CENTER, JLabel.CENTER));
		row2.add(copyToClipboard);
		options.add(row2);
		
		JPanel row3 = new JPanel(new GridLayout(0,2));
		row3.add(createJLabel(LangManager.getItem("config_label_bordersize"), JLabel.CENTER, JLabel.CENTER));
		JPanel row3_2 = new JPanel(new GridLayout(0,2));
		row3_2.add(borderSize);
		JButton colorBtn = new JButton(LangManager.getItem("config_label_color"));
		colorBtn.addActionListener(e -> {
			if(colorChooser == null || !colorChooser.isDisplayable()) {
				int x = (int)((getLocation().getX() + getWidth()/2));
				int y = (int)((getLocation().getY() + getHeight()/2));
				colorChooser = new ColorChooser(sniperInstance.cfg, LangManager.getItem("config_label_bordercolor"), borderColor, null, x, y);
			}
		});
		row3_2.add(colorBtn);
		row3.add(row3_2);
		options.add(row3);
		
		JPanel row4 = new JPanel(new GridLayout(0,2));
		row4.add(createJLabel(LangManager.getItem("config_label_picturelocation"), JLabel.CENTER, JLabel.CENTER));
		row4.add(pictureLocation);
		options.add(row4);

		JPanel row5 = new JPanel(new GridLayout(0,2));
		row5.add(createJLabel(LangManager.getItem("config_label_snapdelay"), JLabel.CENTER, JLabel.CENTER));
		JPanel row5_2 = new JPanel(new GridLayout(0,2));
		row5_2.add(snipeDelay);
		row5.add(row5_2);
		options.add(row5);
		
		JPanel row6 = new JPanel(new GridLayout(0,2));
		row6.add(createJLabel(LangManager.getItem("config_label_openeditor"), JLabel.CENTER, JLabel.CENTER));
		row6.add(openEditor);
		options.add(row6);
		
		JPanel row7 = new JPanel(new GridLayout(0,5));
		row7.add(new JPanel());
		row7.add(new JPanel());
		row7.add(saveButton);
		options.add(row7);
		
		setMinimumSize(new Dimension(512,256));
		add(options);
		pack();
		setVisible(true);
		int w = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
		int h = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
		setLocation((w/2) - getWidth()/2, (h/2) - getHeight()/2);
	}
	
	void msgError(String msg) {
		JOptionPane.showMessageDialog(this, msg,LangManager.getItem("config_sanitation_error"), JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void save() {
		//TODO: Rework sanitation
		boolean _saveToDisk = saveToDisk.isSelected();
		boolean _copyToClipboard = copyToClipboard.isSelected();
		boolean _openEditor = openEditor.isSelected();
		
		String _saveLocation = pictureLocation.getText();
		int _borderSize;
		int _snipeDelay = 0;
		if(Utils.isInteger(borderSize.getText())) {
			_borderSize = Integer.parseInt(borderSize.getText());
		} else {
			msgError(LangManager.getItem("config_sanitation_border_minmax"));
			return;
		}
		
		if(Utils.isInteger(snipeDelay.getText())) {
			_snipeDelay = Integer.parseInt(snipeDelay.getText());
			if(_snipeDelay < 0) {
				msgError(LangManager.getItem("config_sanitation_delay_less0"));
				return;
			}
		}
		
		//ERROR CHECKING
		if(_borderSize < 0) {
			msgError(LangManager.getItem("config_sanitation_border_min"));
			return;
		} else if (_borderSize > maxBorder) {
			msgError(LangManager.getItem("config_sanitation_border_max"));
			return;
		}
		
		String saveLocationFinal = _saveLocation;
		if(saveLocationFinal.contains("%userprofile%")) saveLocationFinal = saveLocationFinal.replace("%userprofile%", System.getenv("USERPROFILE"));
		if(saveLocationFinal.contains("%username%")) saveLocationFinal = saveLocationFinal.replace("%username%", System.getProperty("user.name"));
		
		File saveLocationCheck = new File(saveLocationFinal);
		if(!saveLocationCheck.exists()) {
			Object[] options = {"Okay" , LangManager.getItem("config_sanitation_createdirectory") };
			int msgBox = JOptionPane.showOptionDialog(null,LangManager.getItem("config_sanitation_directory_notexist"), LangManager.getItem("config_sanitation_error"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if(msgBox == 1) {
				File f = new File(saveLocationFinal);
				if(!f.mkdirs()) {
					msgError(LangManager.getItem("config_sanitation_failed_createdirectory"));
				}
			}
			return;
		}

		if(hotKeyButton.hotkey != -1) {
			String hotkeyModifier = "KB";
			if (!hotKeyButton.isKeyboard)
				hotkeyModifier = "M";
			sniperInstance.cfg.set("hotkey", hotkeyModifier + hotKeyButton.hotkey);
		} else {
			sniperInstance.cfg.set("hotkey", "NONE");
		}
		sniperInstance.cfg.set("pictureFolder", _saveLocation);
		sniperInstance.cfg.set("saveToDisk", _saveToDisk + "");
		sniperInstance.cfg.set("borderSize", _borderSize + "");
		sniperInstance.cfg.set("copyToClipboard", _copyToClipboard + "");
		sniperInstance.cfg.set("borderColor", Utils.rgb2hex(borderColor.getColor()));
		sniperInstance.cfg.set("snipeDelay", _snipeDelay + "");
		sniperInstance.cfg.set("openEditor", _openEditor + "");
		sniperInstance.cfg.save();
		
		close();
	}
	
	private boolean closed = false;
	void close() {
		if(!closed) {
			GlobalScreen.removeNativeKeyListener(hotKeyButton);
			GlobalScreen.removeNativeMouseListener(hotKeyButton);
			closed = true;
			if(colorChooser != null) {
				colorChooser.dispose();
				colorChooser = null;
			}
			this.dispose();
			sniperInstance.cfgWnd = null;
		}
	}

	@Override
	public void windowActivated(WindowEvent windowEvent) { }

	@Override
	public void windowClosed(WindowEvent windowEvent) {
		close();
	}

	@Override
	public void windowClosing(WindowEvent windowEvent) {
		close();
	}

	@Override
	public void windowDeactivated(WindowEvent windowEvent) { }

	@Override
	public void windowDeiconified(WindowEvent windowEvent) { }

	@Override
	public void windowIconified(WindowEvent windowEvent) { }

	@Override
	public void windowOpened(WindowEvent windowEvent) { }
}
