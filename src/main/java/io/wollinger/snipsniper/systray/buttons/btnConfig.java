package io.wollinger.snipsniper.systray.buttons;

import java.awt.MenuItem;

import io.wollinger.snipsniper.Main;
import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.configwindow.ConfigWindow;
import io.wollinger.snipsniper.utils.LangManager;

public class btnConfig extends MenuItem{
	
	private static final long serialVersionUID = 5258644643112619509L;

	public btnConfig(Sniper sniperInstance) {
		this.setLabel(LangManager.getItem("menu_config"));
		this.addActionListener(listener -> {
			if(sniperInstance.cfgWnd == null)
				sniperInstance.cfgWnd = new ConfigWindow(sniperInstance);
			else
				sniperInstance.cfgWnd.requestFocus();
		});
		
	}
}
