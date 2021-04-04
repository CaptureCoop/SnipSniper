package io.wollinger.snipsniper.systray.buttons;

import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.LangManager;

import java.awt.MenuItem;

public class btnExit extends MenuItem{
	private static final long serialVersionUID = 1300642542165595046L;

	public btnExit(Sniper sniper) {
		this.setLabel(LangManager.getItem("menu_quit", sniper.cfg.getString("language")));
		this.addActionListener(listener -> System.exit(0));
	}
	
}
