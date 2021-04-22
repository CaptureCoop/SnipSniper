package io.wollinger.snipsniper.systray.buttons;

import java.awt.Desktop;
import java.awt.MenuItem;
import java.io.File;
import java.io.IOException;

import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.LangManager;

public class btnOpenImgFolder extends MenuItem{
	private static final long serialVersionUID = -7145792425566523072L;
	
	public btnOpenImgFolder(Sniper sniperInstance) {
		this.setLabel(LangManager.getItem("menu_open_image_folder"));
		this.addActionListener(listener -> {
			try {
				String path = sniperInstance.cfg.getString("pictureFolder");
				Desktop.getDesktop().open(new File(path));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
	}
	
}
