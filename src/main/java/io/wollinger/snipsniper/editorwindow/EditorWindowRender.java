package io.wollinger.snipsniper.editorwindow;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import io.wollinger.snipsniper.editorwindow.stamps.IStamp;
import io.wollinger.snipsniper.utils.Vector2Int;

public class EditorWindowRender extends JPanel{
	private static final long serialVersionUID = 4283800435207434147L;
	
	EditorWindow editorWnd;
	
	public EditorWindowRender(EditorWindow _wnd) {
		editorWnd = _wnd;
		this.setPreferredSize(new Dimension(_wnd.img.getWidth(), _wnd.img.getHeight()));
	}

	public void paint(Graphics g) {
		g.drawImage(editorWnd.img, 0,0,this.getWidth(),this.getHeight(),this);
		g.drawImage(editorWnd.overdraw, 0,0,this.getWidth(),this.getHeight(),this);
		g.setColor(editorWnd.currentColor.c);
		editorWnd.stamps[editorWnd.selectedStamp].render(g, editorWnd.input);
	}
	
}
