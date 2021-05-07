package io.wollinger.snipsniper.editorwindow;

import java.awt.*;

import javax.swing.JPanel;

public class EditorWindowRender extends JPanel{
	private final EditorWindow editorWnd;
	
	public EditorWindowRender(EditorWindow wnd) {
		editorWnd = wnd;
		if(wnd.getImage() != null)
			setPreferredSize(new Dimension(wnd.getImage().getWidth(), wnd.getImage().getHeight()));
		setDropTarget(new EditorDropTarget(editorWnd));
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHints(editorWnd.getQualityHints());
		if(editorWnd.getImage() != null)
			g2.drawImage(editorWnd.getImage(), 0,0,getWidth(),getHeight(),this);
		if(editorWnd.isStarted())
			editorWnd.getSelectedStamp().render(g2, editorWnd.input, false, false, -1);
		g2.dispose();
		editorWnd.repaint(); //TODO: Find more elegant way to do this, for example a loop
	}
	
}
