package io.wollinger.snipsniper.editorwindow;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

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
		g.setColor(editorWnd.currentColor);
		
		//Later used for a fill tool.
		//Vector2Int startPoint = editorWnd.mouseListener.startPoint;
		//Vector2Int lastPoint = editorWnd.mouseListener.lastPoint;
		//if(startPoint != null && lastPoint != null) g.fillRect(startPoint.x, startPoint.y, lastPoint.x - startPoint.x, lastPoint.y - startPoint.y);
		
		Vector2Int mousePos = editorWnd.listener.mousePos;
		Vector2Int brushSize = new Vector2Int(editorWnd.sniperInstance.cfg.getInt("editorStampWidth"), editorWnd.sniperInstance.cfg.getInt("editorStampHeight"));
		if(mousePos != null) g.fillRect(mousePos.x - brushSize.x/2, mousePos.y - brushSize.y/2, brushSize.x, brushSize.y);
	}
	
}
