package io.wollinger.snipsniper.systray.buttons;

import io.wollinger.snipsniper.Main;
import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.LangManager;

import java.awt.MenuItem;

public class btnExit extends MenuItem{
	private static final long serialVersionUID = 1300642542165595046L;

	public btnExit() {
		this.setLabel(LangManager.getItem("menu_quit"));
		this.addActionListener(listener -> Main.exit());
	}
	
}
