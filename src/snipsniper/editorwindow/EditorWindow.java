package snipsniper.editorwindow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import snipsniper.systray.Sniper;

public class EditorWindow extends JFrame{
	private static final long serialVersionUID = -7363672331227971815L;
	
	BufferedImage img;
	BufferedImage overdraw;
	Sniper sniperInstance;
	EditorWindowRender renderer;
	EditorMouseListener mouseListener;
	
	Color currentColor = new Color(255,255,0,150);
	
	final int X_OFFSET = 8; // This is the offset for X, since the window moves too far to the right otherwise.
	
	public EditorWindow(BufferedImage _img, int _x, int _y, int _w, int _h, String _title, Sniper _sInstance) {
		img = _img;
		sniperInstance = _sInstance;
		overdraw = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		this.setTitle(_title);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setVisible(true);
		int barSize = this.getHeight() - this.getInsets().bottom;

		mouseListener= new EditorMouseListener(this);
		
		renderer = new EditorWindowRender(this);
		
		renderer.addMouseListener(mouseListener);
		renderer.addMouseMotionListener(mouseListener);
		renderer.addMouseWheelListener(mouseListener);
		
		this.add(renderer);
		this.pack();
		this.setLocation(_x - X_OFFSET, _y - barSize - sniperInstance.cfg.borderSize);
	}
	
	public void saveImage() {
		BufferedImage finalImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = finalImg.getGraphics();
		g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), this);
		g.drawImage(overdraw, 0, 0, img.getWidth(), img.getHeight(), this);
		g.dispose();
		sniperInstance.saveImage(finalImg, "_edited");
	}
	
	public void kill() {
		this.dispose();
	}
	
}
