package io.wollinger.snipsniper.editorwindow;

import io.wollinger.snipsniper.editorwindow.stamps.IStamp;

import java.awt.*;

import javax.swing.JPanel;

public class EditorWindowRender extends JPanel{
	private static final long serialVersionUID = 4283800435207434147L;
	
	private final EditorWindow editorWnd;
	
	public EditorWindowRender(EditorWindow _wnd) {
		editorWnd = _wnd;
		this.setPreferredSize(new Dimension(_wnd.getImage().getWidth(), _wnd.getImage().getHeight()));
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
