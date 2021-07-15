package io.wollinger.snipsniper.systray.buttons;

import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.systray.Sniper;
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
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public class btnAbout extends MenuItem{
	private final Sniper sniper;

	private static String html;

	//LOGO USES AGENCY FB BOLD
	public btnAbout(Sniper sniper) {
		this.sniper = sniper;
		setLabel(LangManager.getItem("menu_about"));

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

			ImageIcon icon = new ImageIcon(Icons.icon_taskbar.getScaledInstance(100,100,Image.SCALE_DEFAULT));
			JLabel label = new JLabel(icon);
			label.addMouseListener(new MouseAdapter() {
				int index = 0;

				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					switch(index) {
						case 0: setNewImage(Icons.icon_editor); index++; break;
						case 1: setNewImage(Icons.icon_viewer); index++; break;
						case 2: setNewImage(Icons.icon_console); index++; break;
						case 3: setNewImage(Icons.icon_taskbar); index = 0; break;
					}
				}

				public void setNewImage(BufferedImage image) {
					label.setIcon(new ImageIcon(image.getScaledInstance(100, 100, Image.SCALE_DEFAULT)));
					frame.setIconImage(image);
				}
			});
			panel.add(label);

			JPanel rightSide = new JPanel(new GridLayout(2, 0));
			try {
				Image splash = ImageIO.read(this.getClass().getResource("/splash.png")).getScaledInstance((int)(512F/2.2F),(int)(185F/2.2F),Image.SCALE_DEFAULT);
				rightSide.add(new JLabel(new ImageIcon(splash)));
			} catch (IOException e) {
				e.printStackTrace();
			}

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
