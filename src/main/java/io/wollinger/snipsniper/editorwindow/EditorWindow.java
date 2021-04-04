package io.wollinger.snipsniper.editorwindow;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import io.wollinger.snipsniper.editorwindow.stamps.CircleStamp;
import io.wollinger.snipsniper.editorwindow.stamps.CounterStamp;
import io.wollinger.snipsniper.editorwindow.stamps.CubeStamp;
import io.wollinger.snipsniper.editorwindow.stamps.IStamp;
import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;
import javafx.scene.shape.Circle;

public class EditorWindow extends JFrame{
	private static final long serialVersionUID = -7363672331227971815L;

	private BufferedImage img;
	private BufferedImage overdraw;
	private final Sniper sniperInstance;

	private PBRColor currentColor = new PBRColor(255,255,0,150);
	private final Color censorColor = Color.BLACK;
	
	final static int X_OFFSET = 8; // This is the offset for X, since the window moves too far to the right otherwise.

	private final String title;
	private final String saveLocation;
	private final boolean inClipboard;

	public IStamp[] stamps = new IStamp[3];
	public int selectedStamp = 0;

	InputContainer input = new InputContainer();

	private RenderingHints qualityHints;

	public EditorWindow(BufferedImage _img, int _x, int _y, String _title, Sniper _sInstance, boolean _leftToRight, String saveLocation, boolean inClipboard) {
		img = _img;
		sniperInstance = _sInstance;
		overdraw = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

		this.saveLocation = saveLocation;
		this.inClipboard = inClipboard;
		this.title = _title;

		stamps[0] = new CubeStamp(this);
		stamps[1] = new CounterStamp(_sInstance.cfg);
		stamps[2] = new CircleStamp(_sInstance.cfg);

		qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		qualityHints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );

		refreshTitle();

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setIconImage(Icons.icon_taskbar);
		this.setVisible(true);
		int barSize = this.getHeight() - this.getInsets().bottom;

		EditorListener listener = new EditorListener(this);

		EditorWindowRender renderer = new EditorWindowRender(this);
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
		overdraw = null;
		this.dispose();
	}

	public void setColor(PBRColor color) {
		currentColor = color;
	}

	public Sniper getSniperInstance() {
		return sniperInstance;
	}

	public BufferedImage getImage() {
		return img;
	}

	public BufferedImage getOverdraw() {
		return overdraw;
	}

	public PBRColor getColor() {
		return currentColor;
	}

	public Color getCensorColor() {
		return censorColor;
	}

	public RenderingHints getQualityHints() {
		return qualityHints;
	}

}
