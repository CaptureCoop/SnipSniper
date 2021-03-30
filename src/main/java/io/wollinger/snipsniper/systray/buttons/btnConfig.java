package io.wollinger.snipsniper.systray.buttons;

import java.awt.MenuItem;

import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.configwindow.ConfigWindow;

public class btnConfig extends MenuItem{
	
	private static final long serialVersionUID = 5258644643112619509L;

	public btnConfig(Sniper _sniperInstance) {
		this.setLabel("Config");
		this.addActionListener(listener -> {
			if(_sniperInstance.cfgWnd == null)
				_sniperInstance.cfgWnd = new ConfigWindow(_sniperInstance);
			else
				_sniperInstance.cfgWnd.requestFocus();
		});
		
	}
}
