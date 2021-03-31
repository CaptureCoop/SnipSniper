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

		Vector2Int mousePos = editorWnd.listener.mousePos;

		if(mousePos != null) {
			String type = editorWnd.modeToString(editorWnd.getMode());
			Vector2Int brushSize = new Vector2Int(editorWnd.sniperInstance.cfg.getInt("editorStamp" + type + "Width"), editorWnd.sniperInstance.cfg.getInt("editorStamp" + type + "Height"));

			if(editorWnd.getMode() == EditorWindow.MODE.CUBE) {
				g.fillRect(mousePos.x - brushSize.x / 2, mousePos.y - brushSize.y / 2, brushSize.x, brushSize.y);
			} else if(editorWnd.getMode() == EditorWindow.MODE.CIRCLE) {
				g.drawOval(mousePos.x - brushSize.x / 2, mousePos.y - brushSize.y / 2, brushSize.x, brushSize.y);
			}
		}
	}
	
}
