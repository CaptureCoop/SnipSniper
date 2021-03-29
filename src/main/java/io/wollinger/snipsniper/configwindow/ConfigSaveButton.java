package io.wollinger.snipsniper.configwindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfigSaveButton implements ActionListener{
	
	ConfigWindow cfgWnd;
	
	public ConfigSaveButton(ConfigWindow _cfgWnd) {
		this.cfgWnd = _cfgWnd;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		cfgWnd.save();
	}
	
}
