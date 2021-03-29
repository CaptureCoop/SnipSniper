package io.wollinger.snipsniper.systray.buttons;

import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import io.wollinger.snipsniper.systray.Sniper;

public class btnOpenImgFolder extends MenuItem{

	private static final long serialVersionUID = -7145792425566523072L;
	
	public btnOpenImgFolder(Sniper _sniperInstance) {
		this.setLabel("Open image folder");
		this.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					String path = _sniperInstance.cfg.getString("pictureFolder");
					Desktop.getDesktop().open(new File(path));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
	}
	
}
