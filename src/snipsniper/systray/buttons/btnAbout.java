package snipsniper.systray.buttons;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

import snipsniper.Main;

public class btnAbout extends MenuItem{

	private static final long serialVersionUID = 8081581034217628950L;
	
	public btnAbout() {
		this.setLabel("About");
		this.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,"SnipSniper Version " + Main.VERSION + "\nWritten by Sven Wollinger\nIcons by kiwi_kaiser","About",1);
			}
		});
	}
	
}
