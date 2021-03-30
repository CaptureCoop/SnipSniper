package io.wollinger.snipsniper.systray.buttons;

import io.wollinger.snipsniper.Main;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public class btnAbout extends MenuItem{
	private static final long serialVersionUID = 8081581034217628950L;

	private static String html;

	//LOGO USES AGENCY FB BOLD
	public btnAbout() {
		setLabel("About");

		if(html == null) {
			try {
				loadHTML();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.addActionListener(listener -> {
			JFrame frame = new JFrame();
			frame.setSize(512,256);
			frame.setTitle("About");
			frame.setResizable(true);

			JPanel panel = new JPanel(new GridLayout(1,0));
			
			try {
				Image logo = ImageIO.read(this.getClass().getResource("/res/SnSn.png")).getScaledInstance(100,100,Image.SCALE_DEFAULT);
				JLabel logoLabel = new JLabel(new ImageIcon(logo));
				panel.add(logoLabel);
			} catch (IOException e) {
				e.printStackTrace();
			}

			JPanel rightSide = new JPanel(new GridLayout(2, 0));
			try {
				Image splash = ImageIO.read(this.getClass().getResource("/splash.png")).getScaledInstance((int)(512F/2.2F),(int)(185F/2.2F),Image.SCALE_DEFAULT);
				JLabel splashLabel = new JLabel(new ImageIcon(splash));
				rightSide.add(splashLabel);
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
			frame.setVisible(true);
		});
	}

	public void loadHTML() throws IOException {
		html = "";
		InputStream inputStream = ClassLoader.getSystemClassLoader().getSystemResourceAsStream("about.html");
		InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		BufferedReader in = new BufferedReader(streamReader);

		for (String line; (line = in.readLine()) != null;)
			html += line;

		inputStream.close();
		streamReader.close();
		html = html.replace("%VERSION%", Main.VERSION);
	}
	
}
