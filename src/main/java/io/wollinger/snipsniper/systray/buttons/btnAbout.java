package io.wollinger.snipsniper.systray.buttons;

import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.utils.ConfigHelper;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.LangManager;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public class btnAbout extends JMenuItem{

	private static String html;

	//LOGO USES AGENCY FB BOLD
	public btnAbout() {
		setText(LangManager.getItem("menu_about"));

		try {
			loadHTML();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.addActionListener(listener -> {
			JFrame frame = new JFrame();
			frame.setSize(512,256);
			frame.setTitle("About");
			frame.setResizable(true);
			frame.setIconImage(Icons.icon_taskbar);

			JPanel panel = new JPanel(new GridLayout(1,0));

			int iconSize = 100;
			ImageIcon icon = new ImageIcon(Icons.icon_taskbar.getScaledInstance(iconSize,iconSize,Image.SCALE_DEFAULT));
			JLabel label = new JLabel(icon);
			label.addMouseListener(new MouseAdapter() {
				int index = 0;

				BufferedImage[] icons = {Icons.icon_taskbar, Icons.icon_editor, Icons.icon_viewer, Icons.icon_console};
				HashMap<String, Image> cache = new HashMap<>();

				@Override
				public void mouseReleased(MouseEvent mouseEvent) {
					super.mouseClicked(mouseEvent);

					if(index >= icons.length - 1) index = 0;
					else index++;
					setNewImage(index, iconSize);
				}

				@Override
				public void mousePressed(MouseEvent mouseEvent) {
					setNewImage(index, (int) (iconSize / 1.2F));
				}

				public void setNewImage(int index, int size) {
					Image image;
					String key = index + "_" + size; //We cache those because we really like clicking the icons really fast :^)
					if(cache.containsKey(key)) {
						image = cache.get(key);
					} else {
						image = icons[index].getScaledInstance(size, size, Image.SCALE_DEFAULT);
						cache.put(key, image);
					}
					label.setIcon(new ImageIcon(image));
					frame.setIconImage(image);
				}
			});
			panel.add(label);

			JPanel rightSide = new JPanel(new GridLayout(2, 0));

			rightSide.add(new JLabel(new ImageIcon(Icons.splash.getScaledInstance((int)(Icons.splash.getWidth()/2.2F),(int)(Icons.splash.getHeight()/2.2F),Image.SCALE_DEFAULT))));

			JEditorPane about = new JEditorPane("text/html", html);
			about.setEditable(false);
			about.setOpaque(false);
			about.setSelectionColor(new Color(0,0,0,0));
			about.setSelectedTextColor(Color.black);
			about.addHyperlinkListener(hle -> {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
					try {
						Desktop.getDesktop().browse(new URI(hle.getURL().toString()));
					} catch (IOException | URISyntaxException e) {
						e.printStackTrace();
					}

				}
			});
			rightSide.add(about);

			panel.add(rightSide);

			frame.add(panel);

			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation((int)((size.getWidth()/2) - frame.getWidth()/2), (int)((size.getHeight()/2) - frame.getHeight()/2));

			frame.setVisible(true);
		});
	}

	public void loadHTML() throws IOException {
		StringBuilder htmlTemp = new StringBuilder();
		InputStream inputStream = ClassLoader.getSystemResourceAsStream("about.html");
		if(inputStream == null)
			throw new FileNotFoundException("Could not load about.html inside jar!");
		InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		BufferedReader in = new BufferedReader(streamReader);

		for (String line; (line = in.readLine()) != null;)
			htmlTemp.append(line);

		html = htmlTemp.toString();

		inputStream.close();
		streamReader.close();
		html = html.replace("%VERSION%", SnipSniper.getVersion());
		html = html.replace("%TYPE%", SnipSniper.BUILDINFO.getString(ConfigHelper.BUILDINFO.type));
		html = html.replace("%BUILDDATE%", SnipSniper.BUILDINFO.getString(ConfigHelper.BUILDINFO.builddate));
		html = html.replaceAll("%HASH%", SnipSniper.BUILDINFO.getString(ConfigHelper.BUILDINFO.githash));
		html = html.replace("%ABOUT_PROGRAMMING%", LangManager.getItem("about_programming"));
		html = html.replace("%ABOUT_CD%", LangManager.getItem("about_cd"));
		html = html.replace("%ABOUT_MATH%", LangManager.getItem("about_math"));
	}

}
