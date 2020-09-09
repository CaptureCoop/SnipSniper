package snipsniper.capturewindow;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.TrayIcon.MessageType;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import snipsniper.Icons;
import snipsniper.Utils;
import snipsniper.editorwindow.EditorWindow;
import snipsniper.systray.Sniper;

public class CaptureWindow extends JFrame implements WindowListener{

	private static final long serialVersionUID = 3129624729137795417L;
	Point startPoint;
	Point startPointTotal;
	Point cPoint;
	BufferedImage screenshot = null;
	BufferedImage screenshotTinted = null;
	
	boolean startedCapture = false;
	boolean finishedCapture = false;
	boolean imageSaved = false;
	
	Rectangle bounds = null;
	
	Sniper sniperInstance;
	
	public CaptureWindow(Sniper _sniperInstance) {
		sniperInstance = _sniperInstance;
		if(sniperInstance.cfg.snipeDelay != 0) {
			try {
				Thread.sleep(sniperInstance.cfg.snipeDelay * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		bounds = getTotalBounds();
		this.setUndecorated(true);

		this.addWindowListener(this);
		
		this.setBackground(new Color(0,0,0,0));
		this.addMouseListener(new Mouse(this));
		this.addMouseMotionListener(new MouseMotion(this));
		this.addKeyListener(new Keyboard(this));
	
		this.setIconImage(Icons.icon_taskbar);
	
		this.setVisible(true);
		
		setSize();
		
		this.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				setSize();
			}

			@Override
			public void focusLost(FocusEvent e) {
				setSize();
			}
			
		});
	}
	
	public void setSize() {
		this.setLocation((int)bounds.getX(),(int)bounds.getY());
		this.setSize(bounds.width, bounds.height);
		
		this.requestFocus();
		this.setAlwaysOnTop(true);	
		this.repaint();
	}
	
	Rectangle calcRectangle() {
		int minX = 0, maxX = 0, minY = 0, maxY = 0;
		if(startPoint != null && cPoint != null) {
			minX = Math.min( startPoint.x, cPoint.x);
			maxX = Math.max( startPoint.x, cPoint.x);
			minY = Math.min( startPoint.y, cPoint.y);
			maxY = Math.max( startPoint.y, cPoint.y);
		}
		Rectangle rect = new Rectangle(minX, minY, maxX - minX, maxY - minY);
		return rect;
	}
	
	Rectangle getTotalBounds() {
		Rectangle2D result = new Rectangle2D.Double();
		GraphicsEnvironment localGE = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice gd : localGE.getScreenDevices()) {
			for (GraphicsConfiguration graphicsConfiguration : gd.getConfigurations()) {
				Rectangle2D.union(result, graphicsConfiguration.getBounds(), result);
			}
		}
		return result.getBounds();
	}
	
	void capture() {
		if(!imageSaved) {
			BufferedImage finalImg = null;
			
			this.dispose();
			finishedCapture = true;

			int borderSize = sniperInstance.cfg.borderSize;
			Rectangle captureArea = calcRectangle();

			if(captureArea.width == 0 || captureArea.height == 0) {
				sniperInstance.trayIcon.displayMessage("Error: Screenshot width or height is 0!", "ERROR", MessageType.ERROR);
			} else {
				BufferedImage croppedBuffer = screenshot.getSubimage(captureArea.x, captureArea.y, captureArea.width, captureArea.height);			
				finalImg = new BufferedImage(croppedBuffer.getWidth() + borderSize *2, croppedBuffer.getHeight() + borderSize *2, BufferedImage.TYPE_INT_RGB);
				Graphics g = (Graphics2D) finalImg.getGraphics();
				g.setColor(sniperInstance.cfg.borderColor);
				g.fillRect(0, 0, finalImg.getWidth(),finalImg.getHeight());
				g.drawImage(croppedBuffer, borderSize, borderSize, croppedBuffer.getWidth(), croppedBuffer.getHeight(), this);
				g.dispose();
				
				if(!sniperInstance.saveImage(finalImg, ""));
					this.dispose();
				
				//Copy cropped image to clipboard
				if(sniperInstance.cfg.copyToClipboard)
				    sniperInstance.copyToClipboard(finalImg);
				
			}
			//sniperInstance.trayIcon.displayMessage("Image saved!", "Image saved under: " + file.toString(), MessageType.NONE);
			if(finalImg != null) {
				int posX = (int)cPoint.getX();
				int posY = (int)cPoint.getY();
				
				if(startPointTotal.getX() < cPoint.getX())
					posX = (int)startPointTotal.getX();
				
				if(startPointTotal.getY() < cPoint.getY())
					posY = (int)startPointTotal.getY();
				
				if(sniperInstance.cfg.openEditor)
					new EditorWindow(finalImg, posX - sniperInstance.cfg.borderSize, posY,finalImg.getWidth() - sniperInstance.cfg.borderSize,finalImg.getHeight(), "SnipSniper Editor", sniperInstance);
			}
			sniperInstance.killCaptureWindow();
		}
	}
	
	Rectangle area;
	Point lastPoint = null;
	boolean hasSaved = false;
	public void paint(Graphics g) {
		if(screenshot == null) {
			try {
				Rectangle screenshotRect = new Rectangle((int)bounds.getX(),(int)bounds.getY(), bounds.width, bounds.height);
				screenshot = new Robot().createScreenCapture(screenshotRect);
				screenshotTinted = Utils.copyImage(screenshot);
				Graphics g2 = screenshotTinted.getGraphics();
				g2.setColor(new Color(100,100,100,100));
				g2.fillRect(0, 0, screenshotTinted.getTileWidth(), screenshotTinted.getHeight());
			    g2.dispose();
			    return;
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
	
		if(screenshotTinted != null && !hasSaved) {
			g.drawImage(screenshotTinted, 0,0,bounds.width,bounds.height, this);
			hasSaved = true;
		}
			
		if(area != null && startedCapture) {
			g.drawImage(screenshotTinted, area.x, area.y, area.width, area.height,area.x, area.y, area.width, area.height, this);
			g.drawImage(screenshot, startPoint.x, startPoint.y, cPoint.x, cPoint.y,startPoint.x, startPoint.y, cPoint.x, cPoint.y, this);
		}
	
		if(cPoint != null && startPoint != null)
			area = new Rectangle(startPoint.x, startPoint.y, cPoint.x, cPoint.y);

		lastPoint = cPoint;

	}

	@Override
	public void windowActivated(WindowEvent arg0) { }

	@Override
	public void windowClosed(WindowEvent arg0) { }

	@Override
	public void windowClosing(WindowEvent arg0) {
		//User somehow is attempting to close the capturewindow, delete reference in Sniper instance
		this.sniperInstance.killCaptureWindow();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) { }

	@Override
	public void windowDeiconified(WindowEvent arg0) { }

	@Override
	public void windowIconified(WindowEvent arg0) { }

	@Override
	public void windowOpened(WindowEvent arg0) { }
}
