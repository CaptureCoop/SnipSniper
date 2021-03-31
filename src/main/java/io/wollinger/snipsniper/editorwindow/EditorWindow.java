package io.wollinger.snipsniper.editorwindow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.*;

import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.Icons;

public class EditorWindow extends JFrame{
	private static final long serialVersionUID = -7363672331227971815L;
	
	BufferedImage img;
	BufferedImage overdraw;
	Sniper sniperInstance;
	EditorWindowRender renderer;
	EditorListener listener;
	
	Color currentColor = new Color(255,255,0,150);
	Color censorColor = Color.BLACK;
	
	final static int X_OFFSET = 8; // This is the offset for X, since the window moves too far to the right otherwise.

	private String title;
	private String saveLocation;
	private boolean inClipboard;

	private MODE mode = MODE.CUBE;
	public static enum MODE {CUBE, CIRCLE}

	public EditorWindow(BufferedImage _img, int _x, int _y, String _title, Sniper _sInstance, boolean _leftToRight, String saveLocation, boolean inClipboard) {
		img = _img;
		sniperInstance = _sInstance;
		overdraw = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

		this.saveLocation = saveLocation;
		this.inClipboard = inClipboard;
		this.title = _title;

		refreshTitle();

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setIconImage(Icons.icon_taskbar);
		this.setVisible(true);
		int barSize = this.getHeight() - this.getInsets().bottom;

		listener = new EditorListener(this);
		
		renderer = new EditorWindowRender(this);
		
		renderer.addMouseListener(listener);
		renderer.addMouseMotionListener(listener);
		renderer.addMouseWheelListener(listener);
		this.addKeyListener(listener);
		
		this.add(renderer);
		this.pack();
		
		int borderSize = sniperInstance.cfg.getInt("borderSize");
		if(!_leftToRight) borderSize = -borderSize;
		
		this.setLocation((_x - X_OFFSET) + borderSize, (_y - barSize) + borderSize);
	}

	public void refreshTitle() {
		String newTitle = title;
		if(saveLocation != null && !saveLocation.isEmpty())
			newTitle += " (" + saveLocation + ")";
		if(inClipboard) {
			newTitle += " (Clipboard)";
		}
		setTitle(newTitle);
	}
	
	public void saveImage() {
		BufferedImage finalImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = finalImg.getGraphics();
		g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), this);
		g.drawImage(overdraw, 0, 0, img.getWidth(), img.getHeight(), this);
		g.dispose();
		sniperInstance.saveImage(finalImg, "_edited");
		if(sniperInstance.cfg.getBool("copyToClipboard"))
			sniperInstance.copyToClipboard(finalImg);
	}
	
	public void kill() {
		img = null;
		this.dispose();
	}

	public String modeToString(EditorWindow.MODE mode) {
		if(mode == EditorWindow.MODE.CUBE)
			return "Cube";
		else if(mode == EditorWindow.MODE.CIRCLE)
			return "Circle";
		return null;
	}

	public void setMode(EditorWindow.MODE mode) {
		this.mode = mode;
	}

	public EditorWindow.MODE getMode() {
		return mode;
	}
	
}
