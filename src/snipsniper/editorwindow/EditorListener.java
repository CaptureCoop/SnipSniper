package snipsniper.editorwindow;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import snipsniper.Config;
import snipsniper.utils.Vector2Int;

public class EditorListener implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener{
	
	EditorWindow editorInstance;
	
	Vector2Int startPoint = null;
	
	Vector2Int lastPoint = null;
	
	Vector2Int mousePos = null;
	
	boolean isCTRL = false;
	
	public EditorListener(EditorWindow _editWnd) {
		editorInstance = _editWnd;
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) { }

	@Override
	public void mouseEntered(MouseEvent arg0) { }

	@Override
	public void mouseExited(MouseEvent arg0) { }

	@Override
	public void mousePressed(MouseEvent arg0) {
		//startPoint = new Vector2Int(arg0.getPoint()); Disabled because dragging is currently not finalized
		
		if(arg0.getButton() == 3) {
			editorInstance.saveImage();
			editorInstance.kill();
		}
			
		editorInstance.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {	
		Vector2Int brushSize = new Vector2Int(editorInstance.sniperInstance.cfg.getInt("editorStampWidth"), editorInstance.sniperInstance.cfg.getInt("editorStampHeight"));
		startPoint = new Vector2Int(arg0.getPoint().getX() - brushSize.x/2,arg0.getPoint().getY() - brushSize.y/2);
		lastPoint = new Vector2Int(arg0.getPoint().getX() + brushSize.x/2,arg0.getPoint().getY() + brushSize.y/2);
		save();
		
		startPoint = null;
		lastPoint = null;
		editorInstance.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		//lastPoint = new Vector2Int(arg0.getPoint()); Disabled because dragging is currently not finalized
		mousePos = new Vector2Int(arg0.getPoint());
		editorInstance.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mousePos = new Vector2Int(arg0.getPoint());
		editorInstance.repaint();
	}
	
	public void save() {
		Vector2Int pos = new Vector2Int(lastPoint);
		Vector2Int size = new Vector2Int(startPoint.x - lastPoint.x, startPoint.y - lastPoint.y);
		
		if(startPoint.x < lastPoint.x) {
			pos.x = startPoint.x;
			size.x = lastPoint.x - startPoint.x;
		}
			
		if(startPoint.y < lastPoint.y) {
			pos.y = startPoint.y;
			size.y = lastPoint.y - startPoint.y;
		}
		
		Graphics g = editorInstance.overdraw.getGraphics();
		g.setColor(editorInstance.currentColor);
		g.fillRect(pos.x, pos.y, size.x, size.y);
		g.dispose();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		Config cfg = editorInstance.sniperInstance.cfg;
		
		String dir = "Width";
		if(isCTRL) dir = "Height";
		
		String idSize = "editorStamp" + dir;
		String idSpeed = "editorStamp" + dir + "Speed";
		String idMinimum = "editorStamp" + dir + "Minimum";
		
		int size = cfg.getInt(idSize);
		
		switch(arg0.getWheelRotation()) {
			case 1:
				size -= cfg.getInt(idSpeed);
				break;
			case -1:
				size += cfg.getInt(idSpeed);
				break;
		}
		
		if(size <= cfg.getInt(idMinimum)) size = cfg.getInt(idMinimum);
		cfg.set(idSize, size + "");
		
		editorInstance.repaint();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_CONTROL)
			isCTRL = true;
		
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
			editorInstance.kill();
		
		if(arg0.getKeyCode() == KeyEvent.VK_S) {
			editorInstance.saveImage();
			editorInstance.kill();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_CONTROL)
			isCTRL = false;
	}

	@Override
	public void keyTyped(KeyEvent arg0) { }

}
