package org.snipsniper.systray.buttons;

import org.snipsniper.SnipSniper;
import org.snipsniper.systray.PopupMenuButton;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.Function;
import org.snipsniper.utils.Icons;
import org.snipsniper.LangManager;
import org.snipsniper.utils.Links;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public class btnAbout extends PopupMenuButton {

	private static String html;

	//LOGO USES AGENCY FB BOLD
	public btnAbout(String title, BufferedImage icon, JFrame popup, Function function) {
		super(title, icon, popup, function);

		try {
			loadHTML();
		} catch (IOException e) {
			e.printStackTrace();
		}

		setFunction(new Function() {
			@Override
			public void run() {
				JFrame frame = new JFrame();
				frame.setSize(512,256);
				frame.setTitle("About");
				frame.setResizable(true);
				frame.setIconImage(Icons.getImage("icons/snipsniper.png"));

				JPanel panel = new JPanel(new GridLayout(1,0));

				JPanel iconPanel = new JPanel(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0; gbc.gridy = 0;
				int iconSize = 100;
				ImageIcon icon = new ImageIcon(Icons.getImage("icons/snipsniper.png").getScaledInstance(iconSize,iconSize,Image.SCALE_DEFAULT));
				JLabel label = new JLabel(icon);
				label.addMouseListener(new MouseAdapter() {
					int index = 0;

					BufferedImage[] icons = {Icons.getImage("icons/snipsniper.png"), Icons.getImage("icons/editor.png"), Icons.getImage("icons/viewer.png"), Icons.getImage("icons/console.png")};
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
				iconPanel.add(label, gbc);
				gbc.gridy = 1;
				gbc.insets = new Insets(20, 0, 0, 0);
				JButton buyCoffee = new JButton("Buy us a coffee");
				buyCoffee.addActionListener(e -> Links.openLink(Links.KOFI));
				BufferedImage kofiIcon = Icons.getImage("icons/kofi.png");
				buyCoffee.setIcon(new ImageIcon(kofiIcon.getScaledInstance(kofiIcon.getWidth()/8, kofiIcon.getHeight()/8, Image.SCALE_DEFAULT)));
				buyCoffee.setHorizontalTextPosition(SwingConstants.LEFT);

				iconPanel.add(buyCoffee, gbc);
				panel.add(iconPanel);

				JPanel rightSide = new JPanel(new GridLayout(2, 0));

				BufferedImage splash = Icons.getImage("splash.png");
				rightSide.add(new JLabel(new ImageIcon(splash.getScaledInstance((int)(splash.getWidth()/2.2F),(int)(splash.getHeight()/2.2F),Image.SCALE_DEFAULT))));

				JEditorPane about = new JEditorPane("text/html", html);
				about.setEditable(false);
				about.setOpaque(false);
				about.setSelectionColor(new Color(0,0,0,0));
				about.setSelectedTextColor(Color.black);
				about.addHyperlinkListener(hle -> {
					if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
						Links.openLink(hle.getURL().toString());
					}
				});
				rightSide.add(about);

				panel.add(rightSide);

				frame.add(panel);

				Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
				frame.setLocation((int)((size.getWidth()/2) - frame.getWidth()/2), (int)((size.getHeight()/2) - frame.getHeight()/2));

				frame.setVisible(true);
			}
		});
	}

	public void loadHTML() throws IOException {
		StringBuilder htmlTemp = new StringBuilder();
		InputStream inputStream = ClassLoader.getSystemResourceAsStream("org/snipsniper/resources/about.html");
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
		String theme = SnipSniper.getConfig().getString(ConfigHelper.MAIN.theme);
		String color = "";
		switch(theme) {
			case "dark": color = "white"; break;
			case "light": color = "black"; break;
		}
		html = html.replace("%LINK_COLOR%", color);
	}

}
