package io.wollinger.snipsniper.systray.buttons;

import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.utils.LangManager;

import java.awt.MenuItem;

public class btnExit extends MenuItem{

	public btnExit() {
		setLabel(LangManager.getItem("menu_quit"));
		addActionListener(listener -> SnipSniper.exit(false));
	}
	
}
