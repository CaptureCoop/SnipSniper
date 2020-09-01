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
import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import snipsniper.Icons;
import snipsniper.Utils;
import snipsniper.systray.Sniper;

public class CaptureWindow extends JFrame implements WindowListener{

	private static final long serialVersionUID = 3129624729137795417L;
	Point startPoint;
	Point cPoint;
	BufferedImage screenshot;
	BufferedImage screenshotTinted;
	
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
	
		this.setIconImage(Icons.icon_highres);
	
		this.setVisible(true);
		
		this.setLocation((int)bounds.getX(),(int)bounds.getY());
		this.setSize(bounds.width, bounds.height);
		
		this.requestFocus();
		this.setAlwaysOnTop(true);		
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
			this.dispose();
			finishedCapture = true;

			int borderSize = sniperInstance.cfg.borderSize;
			Rectangle captureArea = calcRectangle();

			if(captureArea.width == 0 || captureArea.height == 0) {
				sniperInstance.trayIcon.displayMessage("Error: Screenshot width or height is 0!", "ERROR", MessageType.ERROR);
			} else {
				BufferedImage croppedBuffer = screenshot.getSubimage(captureArea.x, captureArea.y, captureArea.width, captureArea.height);			
				BufferedImage finalImg = new BufferedImage(croppedBuffer.getWidth() + borderSize *2, croppedBuffer.getHeight() + borderSize *2, BufferedImage.TYPE_INT_RGB);
				Graphics g = (Graphics2D) finalImg.getGraphics();
				g.setColor(sniperInstance.cfg.borderColor);
				g.fillRect(0, 0, finalImg.getWidth(),finalImg.getHeight());
				g.drawImage(croppedBuffer, borderSize, borderSize, croppedBuffer.getWidth(), croppedBuffer.getHeight(), this);
				g.dispose();
				
				LocalDateTime now = LocalDateTime.now();  
				String filename = now.toString().replace(".", "_").replace(":", "_");
				filename += ".png";
				File path = new File(sniperInstance.cfg.pictureFolder);
				File file = new File(sniperInstance.cfg.pictureFolder + filename);
				try {
					if(sniperInstance.cfg.savePictures) {
						if(!path.exists()) path.mkdirs();
						if(file.createNewFile()) {
							ImageIO.write(finalImg, "png", file);
							imageSaved = true;
						}
					}
				} catch (IOException e) {
					this.dispose();
					JOptionPane.showMessageDialog(null, "Could not save image to \"" + file.toString()  + "\"!" , "Error", 1);
					e.printStackTrace();
				}
				//Copy cropped image to clipboard
				if(sniperInstance.cfg.copyToClipboard) {
				    ImageSelection imgSel = new ImageSelection(finalImg);
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
				}
				
			}
			//sniperInstance.trayIcon.displayMessage("Image saved!", "Image saved under: " + file.toString(), MessageType.NONE);
			sniperInstance.killCaptureWindow();
		}
	}
	
	Rectangle area;
	Point lastPoint = null;
	public void paint(Graphics g) {
		
		if(screenshot == null) {
			try {
				System.out.println("Taking Screenshot");
				screenshot = new Robot().createScreenCapture(new Rectangle((int)bounds.getX(),(int)bounds.getY(), bounds.width, bounds.height));
				screenshotTinted = Utils.copyImage(screenshot);
				Graphics stG = screenshotTinted.getGraphics();
				stG.setColor(new Color(100,100,100,100));
				stG.fillRect(0, 0, bounds.width, bounds.height);;
				stG.dispose();
				g.drawImage(screenshotTinted, 0,0, bounds.width, bounds.height, this);
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
		
		if(area != null && startedCapture) {
			g.drawImage(screenshotTinted, area.x, area.y, area.width, area.height,area.x, area.y, area.width, area.height, this);
			//Optional way of drawing the tint, doesnt feel very efficient though.
			//g.drawImage(screenshotTinted, (int)lastPoint.getX(), (int)lastPoint.getY(), (int)startPoint.getX(),(int)startPoint.getY(),(int)lastPoint.getX(), (int)lastPoint.getY(), (int)startPoint.getX(),(int)startPoint.getY(),  this);
			g.drawImage(screenshot, startPoint.x, startPoint.y, cPoint.x, cPoint.y,startPoint.x, startPoint.y, cPoint.x, cPoint.y, this);
		}

		if(cPoint != null)
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
