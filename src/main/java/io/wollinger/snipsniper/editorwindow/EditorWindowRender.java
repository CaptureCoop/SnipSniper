package io.wollinger.snipsniper.editorwindow;

import java.awt.*;

import javax.swing.JPanel;

public class EditorWindowRender extends JPanel{
	private static final long serialVersionUID = 4283800435207434147L;
	
	private final EditorWindow editorWnd;
	
	public EditorWindowRender(EditorWindow wnd) {
		editorWnd = wnd;
		this.setPreferredSize(new Dimension(wnd.getImage().getWidth(), wnd.getImage().getHeight()));
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHints(editorWnd.getQualityHints());
		g2.drawImage(editorWnd.getImage(), 0,0,this.getWidth(),this.getHeight(),this);
		editorWnd.getSelectedStamp().render(g2, editorWnd.input, false, false, -1);
		g2.dispose();
		editorWnd.repaint(); //TODO: Find more elegant way to do this, for example a loop
	}
	
}
