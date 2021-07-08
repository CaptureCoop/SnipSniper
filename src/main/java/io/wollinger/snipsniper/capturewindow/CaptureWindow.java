package io.wollinger.snipsniper.capturewindow;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.logging.Level;

import javax.swing.JFrame;

import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;
import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.*;
import org.apache.commons.lang3.SystemUtils;

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

		if(SystemTray.isSupported()) sniperInstance.trayIcon.setImage(Icons.alt_icons[sniperInstance.profileID]);
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
			public void focusGained(FocusEvent focusEvent) { }

			@Override
			public void focusLost(FocusEvent focusEvent) {
				setSize();
			}
		});
     	setVisible(true);
		setSize();
		if(SystemUtils.IS_OS_LINUX) GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
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
	BufferedImage selectBufferImage;
	BufferedImage spyglassBufferImage;
	Rectangle spyglassRectangle;

	Rectangle2D redraw = new Rectangle2D.Double();

	public void paint(Graphics g) {
		boolean directDraw = sniperInstance.cfg.getBool(ConfigHelper.PROFILE.directDraw);
		//TODO: Direct draw runs horribly on linux. Check out why?

		if(!directDraw && bounds != null && bufferImage == null && selectBufferImage == null) {
			//We are only setting this once, since the size of bounds should not really change
			bufferImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
			selectBufferImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
		}

		if(spyglassBufferImage == null) {
			spyglassBufferImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
			Graphics gaga = spyglassBufferImage.getGraphics();
			gaga.setColor(new Color(200, 200, 200, 100));
			gaga.fillRect(0,0, spyglassBufferImage.getWidth(), spyglassBufferImage.getHeight());
			gaga.dispose();
		}

		Graphics2D globalBuffer = (Graphics2D) bufferImage.getGraphics();
		globalBuffer.setRenderingHints(qualityHints);

		Graphics2D selectBuffer = (Graphics2D) selectBufferImage.getGraphics();
		selectBuffer.setRenderingHints(qualityHints);

		Graphics2D spyglassBuffer = (Graphics2D) spyglassBufferImage.getGraphics();
		spyglassBuffer.setRenderingHints(qualityHints);

		if(directDraw) {
			globalBuffer = (Graphics2D) g;
			selectBuffer = (Graphics2D) g;
			spyglassBuffer = (Graphics2D) g;
		}

		Rectangle rr = redraw.getBounds();
		globalBuffer.drawImage(screenshotTinted, rr.x, rr.y, rr.width, rr.height, rr.x, rr.y, rr.width, rr.height, this);

		redraw = new Rectangle2D.Double();

		if(screenshot != null) {
			if((screenshotTinted != null && !hasSaved && bounds != null) || SystemUtils.IS_OS_LINUX) {
				if(SnipSniper.getConfig().getBool(ConfigHelper.MAIN.debug)) {
					LogManager.log(sniperInstance.getID(), "About to render image: " + screenshotTinted, Level.INFO);
					LogManager.log(sniperInstance.getID(), "Frame Visible: " + isVisible(), Level.INFO);
				}

				globalBuffer.drawImage(screenshotTinted, 0,0, bounds.width,bounds.height, this);
				addRectangle(redraw, bounds);
				System.out.println("B0 " + redraw.getBounds());
				if(SnipSniper.getConfig().getBool(ConfigHelper.MAIN.debug)) {
					LogManager.log(sniperInstance.getID(), "Rendered tinted background. More Info: ", Level.INFO);
					LogManager.log(sniperInstance.getID(), "Image rendered:        " + screenshotTinted.toString(), Level.INFO);
					LogManager.log(sniperInstance.getID(), "Frame Visible: " + isVisible(), Level.INFO);
				}
				hasSaved = true;
			}

			if(area != null && cPoint != null && startedCapture) {
				selectBuffer.drawImage(screenshot, startPoint.x, startPoint.y, cPoint.x, cPoint.y,startPoint.x, startPoint.y, cPoint.x, cPoint.y, this);
			}

			if(cPoint != null && startPoint != null) {
				area = new Rectangle(startPoint.x, startPoint.y, cPoint.x, cPoint.y);
				addRectangle(redraw, area);
				System.out.println("B1 " + redraw.getBounds() + "->" + area);
			}

			if(cPoint != null && area != null) {
				globalBuffer.drawImage(selectBufferImage, area.x, area.y, area.width, area.height, area.x, area.y, area.width, area.height, this);
				spyglassRectangle = new Rectangle(cPoint.x - spyglassBufferImage.getWidth(), cPoint.y - spyglassBufferImage.getHeight(), cPoint.x, cPoint.y);
				globalBuffer.drawImage(spyglassBufferImage, spyglassRectangle.x, spyglassRectangle.y, this);
				addRectangle(redraw, spyglassRectangle);
				System.out.println("B2 " + redraw.getBounds() + "->" + spyglassRectangle + "\n");
			}

			Rectangle r = redraw.getBounds();
			g.drawImage(bufferImage, r.x, r.y, r.width, r.height, r.x, r.y, r.width, r.height, this);
			DrawUtils.drawRect(g, new Rectangle(r.x, r.y, r.width - r.x, r.height - r.y));
			lastPoint = cPoint;
		} else {
			LogManager.log(sniperInstance.getID(), "WARNING: Screenshot is null when trying to render. Trying again.", Level.WARNING);
			repaint();
		}

		globalBuffer.dispose();
		selectBuffer.dispose();
		spyglassBuffer.dispose();
	}

	public void addRectangle(Rectangle2D addTo, Rectangle2D toAdd) {
		Double newX = Math.min(addTo.getX(), toAdd.getX());
		Double newY = Math.min(addTo.getY(), toAdd.getY());

		if(newX == 0)
			newX = toAdd.getX();
		if(newY == 0)
			newY = toAdd.getY();

		Rectangle2D.union(addTo, toAdd, addTo);
		addTo.setRect(newX, newY, addTo.getWidth(), addTo.getHeight());
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
