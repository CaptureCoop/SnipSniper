package io.wollinger.snipsniper.editorwindow;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.editorwindow.stamps.*;
import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.Utils;

public class EditorWindow extends JFrame{
	private static final long serialVersionUID = -7363672331227971815L;

	private final String id;
	private BufferedImage img;
	private final Config config;

	private final Color censorColor = Color.BLACK;
	
	final static int X_OFFSET = 8; // This is the offset for X, since the window moves too far to the right otherwise.

	private final String title;
	private final String saveLocation;
	private final boolean inClipboard;

	private final IStamp[] stamps = new IStamp[5];
	private int selectedStamp = 0;

	InputContainer input = new InputContainer();
	private EditorWindowRender editorWindowRender;
	private EditorListener editorListener;

	private final RenderingHints qualityHints;

	public static final String FILENAME_MODIFIER = "_edited";

	public boolean isDirty = false;

	private boolean isStandalone;
	private boolean isStarted = false;

	public EditorWindow(String id, BufferedImage img, int x, int y, String title, Config config, boolean leftToRight, String saveLocation, boolean inClipboard, boolean isStandalone) {
		this.id = id;
		this.img = img;
		this.config = config;

		this.saveLocation = saveLocation;
		this.inClipboard = inClipboard;
		this.title = title;
		this.isStandalone = isStandalone;

		stamps[0] = new CubeStamp(this);
		stamps[1] = new CounterStamp(config);
		stamps[2] = new CircleStamp(this, config);
		stamps[3] = new SimpleBrush(this);
		stamps[4] = new TextStamp(config);

		qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		refreshTitle();

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setResizable(false);
		if (isStandalone)
			this.setIconImage(Icons.icon_editor);
		else
			this.setIconImage(Icons.icon_taskbar);
		int barSize = this.getHeight() - this.getInsets().bottom;

		editorWindowRender = new EditorWindowRender(this);
		this.setFocusTraversalKeysEnabled(false);

		this.add(editorWindowRender);
		this.pack();

		if(img == null) {
			BufferedImage dropImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
			Graphics g = dropImage.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0,0,dropImage.getWidth(), dropImage.getHeight());
			g.setColor(Color.BLACK);
			String string = "Drop image here";
			g.setFont(new Font("Arial", Font.BOLD, 20));
			int width = g.getFontMetrics().stringWidth(string);
			g.drawString(string, dropImage.getWidth()/2 - width/2, 256);
			g.dispose();
			setImage(dropImage, false);
		} else {
			start();
		}
		
		int borderSize = config.getInt("borderSize");
		if(!leftToRight) borderSize = -borderSize;
		
		this.setLocation((x - X_OFFSET) + borderSize, (y - barSize) + borderSize);

		GraphicsEnvironment localGE = GraphicsEnvironment.getLocalGraphicsEnvironment();
		boolean found = false;
		GraphicsConfiguration bestMonitor = null;
		final int SAFETY_OFFSET_X = 10; //This prevents this setup not working if you do a screenshot on the top left, which would cause the location not to be in any bounds
		for (GraphicsDevice gd : localGE.getScreenDevices()) {
			for (GraphicsConfiguration graphicsConfiguration : gd.getConfigurations()) {
				if(!found) {
					Rectangle bounds = graphicsConfiguration.getBounds();

					Point testLocation = new Point((int) (getLocation().getX() + SAFETY_OFFSET_X), (int) getLocation().getY());

					if (bounds.contains(testLocation))
						found = true;

					if (testLocation.getX() > bounds.getX() && testLocation.getX() < (bounds.getX() + bounds.getWidth()) && bestMonitor == null) {
						bestMonitor = graphicsConfiguration;
					}
				}
			}
		}
		if(!found && bestMonitor != null)
			setLocation((int) getLocation().getX(), bestMonitor.getBounds().y);
		setVisible(true);
	}

	public void start() {
		editorListener = new EditorListener(this);
		editorWindowRender.addMouseListener(editorListener);
		editorWindowRender.addMouseMotionListener(editorListener);
		editorWindowRender.addMouseWheelListener(editorListener);
		addKeyListener(editorListener);
		isStarted = true;
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
		g.dispose();
		Utils.saveImage(id, finalImg, FILENAME_MODIFIER, config);
		if(config.getBool("copyToClipboard"))
			Utils.copyToClipboard(id,finalImg);
	}
	
	public void kill() {
		img = null;
		this.dispose();
	}

	public void setSelectedStamp(int i) {
		selectedStamp = i;
	}

	public boolean isStarted () {
		return isStarted;
	}

	public EditorListener getEditorListener() {
		return editorListener;
	}

	public IStamp getSelectedStamp() {
		return stamps[selectedStamp];
	}

	public IStamp[] getStamps() {
		return stamps;
	}

	public void setImage(BufferedImage image, boolean resetHistory) {
		LogManager.log(id, "Setting new Image", Level.INFO);
		this.img = image;

		Insets insets = getInsets();
		setSize(insets.left + insets.right + image.getWidth(), insets.bottom + insets.top + image.getHeight());

		if(getEditorListener() != null && resetHistory) {
			getEditorListener().resetHistory();
		}
	}

	public EditorWindowRender getEditorWindowRender (){
		return editorWindowRender;
	}

	public Config getConfig() {
		return config;
	}

	public BufferedImage getImage() {
		return img;
	}

	public Color getCensorColor() {
		return censorColor;
	}

	public RenderingHints getQualityHints() {
		return qualityHints;
	}

	public String getID() {
		return id;
	}

}
