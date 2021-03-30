package io.wollinger.snipsniper.systray.buttons;

import java.awt.MenuItem;

public class btnExit extends MenuItem{
	private static final long serialVersionUID = 1300642542165595046L;

	public btnExit() {
		this.setLabel("Quit");
		this.addActionListener(listener -> System.exit(0));
	}
	
}
