package io.wollinger.snipsniper.systray.buttons;

import java.awt.MenuItem;
import javax.swing.JOptionPane;

import io.wollinger.snipsniper.Main;

public class btnAbout extends MenuItem{

	private static final long serialVersionUID = 8081581034217628950L;
	
	public btnAbout() {
		this.setLabel("About");
		this.addActionListener(listener -> JOptionPane.showMessageDialog(null,"SnipSniper Version " + Main.VERSION + "\nWritten by Sven Wollinger\nIcons by kiwi_kaiser","About", JOptionPane.INFORMATION_MESSAGE));
	}
	
}
