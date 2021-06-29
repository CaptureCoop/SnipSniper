package io.wollinger.snipsniper.capturewindow;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.logging.Level;

import javax.swing.JFrame;

import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;
import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.ConfigHelper;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.LogManager;
import io.wollinger.snipsniper.utils.Utils;

public class CaptureWindow extends JFrame implements WindowListener{
	Sniper sniperInstance;
	private final RenderingHints qualityHints;

	Point startPoint;
	Point startPointTotal;
	Point cPoint;
	Point cPointAlt;
	private Rectangle bounds = null;
	
	public BufferedImage screenshot = null;
	public BufferedImage screenshotTinted = null;
	
	public boolean startedCapture = false;
	public boolean isRunning = true;

	public CaptureWindow(Sniper sniperInstance) {
		this.sniperInstance = sniperInstance;

		qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		sniperInstance.trayIcon.setImage(Icons.alt_icons[sniperInstance.profileID]);
		if(sniperInstance.cfg.getInt(ConfigHelper.PROFILE.snipeDelay) != 0) {
			try {
				Thread.sleep(sniperInstance.cfg.getInt(ConfigHelper.PROFILE.snipeDelay) * 1000L);
			} catch (InterruptedException e) {
				LogManager.log(sniperInstance.getID(), "There was an error with the delay! Message: " + e.getMessage(), Level.SEVERE);
				LogManager.log(sniperInstance.getID(), "More info: " + Arrays.toString(e.getStackTrace()), Level.SEVERE);
			}
		}
		
		screenshot();

		setUndecorated(true);
		setIconImage(Icons.icon_taskbar);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		CaptureWindowListener listener = new CaptureWindowListener(this);
		addWindowListener(this);
		addMouseListener(listener);
		addMouseMotionListener(listener);
		addKeyListener(listener);
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent focusEvent) {
				setSize();
			}

			@Override
			public void focusLost(FocusEvent focusEvent) {
				setSize();
			}
		});
     
	   loop(); 
	}
	
	
	public void loop() {
		Thread thread = new Thread(() -> {
			final double nsPerTick = 1000000000D / sniperInstance.cfg.getInt(ConfigHelper.PROFILE.maxFPS);
			long lastTime = System.nanoTime();
			long lastTimer = System.currentTimeMillis();
			double delta = 0;
			boolean screenshotDone = false;

			while (isRunning) {
				if (screenshotDone) {
					if(!isVisible()) setVisible(true);
					setSize();
					specialRepaint();
				}
				if (screenshot != null && screenshotTinted != null && !screenshotDone) screenshotDone = true;

				long now = System.nanoTime();
				delta += (now - lastTime) / nsPerTick;
				lastTime = now;

				while (delta >= 1) {
					delta -= 1;
					if (screenshotDone) specialRepaint();
				}

				if (System.currentTimeMillis() - lastTimer >= 1000)
					lastTimer += 1000;
			}
		});
		thread.start();
	}
	
	public void specialRepaint() {
		if(area != null) {
			final Rectangle rect = area;
			
			int x = Math.min( rect.x, rect.width);
			int y = Math.min( rect.y, rect.height);
			int width = Math.max(rect.x, rect.width);
			int height = Math.max(rect.y, rect.height);

			repaint(x,y,width,height);
		} else {
			repaint();
		}

	}
	
	public synchronized void screenshot() {
		bounds = getTotalBounds();
		Rectangle screenshotRect = new Rectangle((int)bounds.getX(),(int)bounds.getY(), bounds.width, bounds.height);
		try {
			screenshot = new Robot().createScreenCapture(screenshotRect);
		} catch (AWTException e) {
			LogManager.log(sniperInstance.getID(), "Couldn't take screenshot. Message: " + e.getMessage(), Level.SEVERE);
			e.printStackTrace();
		}
		screenshotTinted = Utils.copyImage(screenshot);
		Graphics g2 = screenshotTinted.getGraphics();
		g2.setColor(new Color(100,100,100,100));
		g2.fillRect(0, 0, screenshotTinted.getTileWidth(), screenshotTinted.getHeight());
	    g2.dispose();
	}
	
	public void setSize() {
		setLocation((int)bounds.getX(),(int)bounds.getY());
		setSize(bounds.width, bounds.height);
		requestFocus();
		setAlwaysOnTop(true);
		repaint();
	}
	
	Rectangle calcRectangle() {
		int minX = 0, maxX = 0, minY = 0, maxY = 0;
		if(startPoint != null && cPoint != null) {
			minX = Math.min( startPoint.x, cPoint.x);
			maxX = Math.max( startPoint.x, cPoint.x);
			minY = Math.min( startPoint.y, cPoint.y);
			maxY = Math.max( startPoint.y, cPoint.y);
		}
		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
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
		BufferedImage finalImg;
		isRunning = false;
		dispose();

		int borderSize = sniperInstance.cfg.getInt(ConfigHelper.PROFILE.borderSize);
		Rectangle captureArea = calcRectangle();

		if (captureArea.width == 0 || captureArea.height == 0) {
			sniperInstance.trayIcon.displayMessage("Error: Screenshot width or height is 0!", "ERROR", MessageType.ERROR);
			sniperInstance.killCaptureWindow();
			return;
		}

		BufferedImage croppedBuffer = screenshot.getSubimage(captureArea.x, captureArea.y, captureArea.width, captureArea.height);
		finalImg = new BufferedImage(croppedBuffer.getWidth() + borderSize *2, croppedBuffer.getHeight() + borderSize *2, BufferedImage.TYPE_INT_RGB);
		Graphics g = finalImg.getGraphics();
		g.setColor(sniperInstance.cfg.getColor(ConfigHelper.PROFILE.borderColor));
		g.fillRect(0, 0, finalImg.getWidth(),finalImg.getHeight());
		g.drawImage(croppedBuffer, borderSize, borderSize, croppedBuffer.getWidth(), croppedBuffer.getHeight(), this);
		g.dispose();

		String finalLocation = null;
		boolean inClipboard = false;

		if(sniperInstance.cfg.getBool(ConfigHelper.PROFILE.saveToDisk)) {
			finalLocation = Utils.saveImage(sniperInstance.getID(), finalImg, "", sniperInstance.cfg);
		}

		if(sniperInstance.cfg.getBool(ConfigHelper.PROFILE.copyToClipboard)) {
			Utils.copyToClipboard(sniperInstance.getID(), finalImg);
			inClipboard = true;
		}

		int posX = (int) cPointAlt.getX();
		int posY = (int) cPointAlt.getY();
		boolean leftToRight = false;

		if (!(startPointTotal.getX() > cPointAlt.getX())) {
			posX -= finalImg.getWidth();
			leftToRight = true;
		}
		if (!(startPointTotal.getY() > cPointAlt.getY())) {
			posY -= finalImg.getHeight();
			leftToRight = true;
		}
		if (sniperInstance.cfg.getBool(ConfigHelper.PROFILE.openEditor)) {
			new SCEditorWindow("EDI" + sniperInstance.profileID, finalImg, posX, posY, "SnipSniper Editor", sniperInstance.cfg, leftToRight, finalLocation, inClipboard, false);
		}

		sniperInstance.killCaptureWindow();
	}
	
	public Rectangle area;
	Point lastPoint = null;
	boolean hasSaved = false;
	BufferedImage bufferImage;

	public void paint(Graphics g) {
		if(bounds != null) {
			bufferImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
		}
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHints(qualityHints);

		Graphics2D gBuffer = (Graphics2D)bufferImage.getGraphics();
		gBuffer.setRenderingHints(qualityHints);

		if(screenshot != null && bufferImage != null) {
			if(screenshotTinted != null && !hasSaved && bounds != null) {
				if(SnipSniper.getConfig().getBool(ConfigHelper.MAIN.debug)) {
					LogManager.log(sniperInstance.getID(), "About to render image: " + screenshotTinted, Level.INFO);
					LogManager.log(sniperInstance.getID(), "Frame Visible: " + isVisible(), Level.INFO);
				}
				g2.drawImage(screenshotTinted, 0,0, bounds.width, bounds.height, this);
				if(SnipSniper.getConfig().getBool(ConfigHelper.MAIN.debug)) {
					LogManager.log(sniperInstance.getID(), "Rendered tinted background. More Info: ", Level.INFO);
					LogManager.log(sniperInstance.getID(), "Image rendered:        " + screenshotTinted.toString(), Level.INFO);
					LogManager.log(sniperInstance.getID(), "Frame Visible: " + isVisible(), Level.INFO);
				}
				hasSaved = true;
			}

			if(area != null && startedCapture) {
				Graphics use = gBuffer;

				boolean directDraw = sniperInstance.cfg.getBool(ConfigHelper.PROFILE.directDraw);
				if(directDraw)
					use = g2;

				use.drawImage(screenshotTinted, area.x, area.y, area.width, area.height,area.x, area.y, area.width, area.height, this);
				use.drawImage(screenshot, startPoint.x, startPoint.y, cPoint.x, cPoint.y,startPoint.x, startPoint.y, cPoint.x, cPoint.y, this);

				if(!directDraw)
					g2.drawImage(bufferImage, area.x, area.y, area.width, area.height,area.x, area.y, area.width, area.height, this);
			}


			if(cPoint != null && startPoint != null)
				area = new Rectangle(startPoint.x, startPoint.y, cPoint.x, cPoint.y);
	
			lastPoint = cPoint;
		} else {
			LogManager.log(sniperInstance.getID(), "WARNING: Screenshot is null when trying to render. Trying again.", Level.WARNING);
			repaint();
		}
		g2.dispose();
		gBuffer.dispose();
	}

	@Override
	public void windowActivated(WindowEvent windowEvent) { }

	@Override
	public void windowClosed(WindowEvent windowEvent) { }

	@Override
	public void windowClosing(WindowEvent windowEvent) {
		sniperInstance.killCaptureWindow();
	}

	@Override
	public void windowDeactivated(WindowEvent windowEvent) { }

	@Override
	public void windowDeiconified(WindowEvent windowEvent) { }

	@Override
	public void windowIconified(WindowEvent windowEvent) { }

	@Override
	public void windowOpened(WindowEvent windowEvent) { }
}
