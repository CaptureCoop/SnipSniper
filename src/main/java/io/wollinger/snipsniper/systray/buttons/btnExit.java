package io.wollinger.snipsniper.systray.buttons;

import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.utils.LangManager;

import java.awt.MenuItem;

public class btnExit extends MenuItem{
	private static final long serialVersionUID = 1300642542165595046L;

	public btnExit() {
		setLabel(LangManager.getItem("menu_quit"));
		addActionListener(listener -> SnipSniper.exit());
	}
	
}
