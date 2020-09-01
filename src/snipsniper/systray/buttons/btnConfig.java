package snipsniper.systray.buttons;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import snipsniper.config.ConfigWindow;
import snipsniper.systray.Sniper;

public class btnConfig extends MenuItem{
	
	private static final long serialVersionUID = 5258644643112619509L;

	public btnConfig(Sniper _sniperInstance) {
		this.setLabel("Config");
		this.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				if(_sniperInstance.cfgWnd == null)
					_sniperInstance.cfgWnd = new ConfigWindow(_sniperInstance);
				else
					_sniperInstance.cfgWnd.requestFocus();
			}
		});
		
	}
}
