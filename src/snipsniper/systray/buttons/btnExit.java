package snipsniper.systray.buttons;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class btnExit extends MenuItem{

	private static final long serialVersionUID = 1300642542165595046L;

	public btnExit() {
		this.setLabel("Quit");
		this.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}
	
}
