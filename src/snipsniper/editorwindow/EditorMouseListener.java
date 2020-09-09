package snipsniper.editorwindow;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import snipsniper.Vector2Int;

public class EditorMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener{
	
	EditorWindow editorInstance;
	
	Vector2Int startPoint = null;
	
	Vector2Int lastPoint = null;
	
	Vector2Int mousePos = null;
	
	Vector2Int brushSize = new Vector2Int(20,20);
	
	public EditorMouseListener(EditorWindow _editWnd) {
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
		//startPoint = new Vector2Int(arg0.getPoint());
		
		if(arg0.getButton() == 3)
			editorInstance.saveImage();
		
		startPoint = new Vector2Int(arg0.getPoint().getX() - brushSize.x/2,arg0.getPoint().getY() - brushSize.y/2);
		lastPoint = new Vector2Int(arg0.getPoint().getX() + brushSize.x/2,arg0.getPoint().getY() + brushSize.y/2);
		
		save();
		editorInstance.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {	
		//save();
		
		startPoint = null;
		lastPoint = null;
		editorInstance.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		lastPoint = new Vector2Int(arg0.getPoint());
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
		switch(arg0.getWheelRotation()) {
		case -1:
			brushSize.x--;
			break;
		case 1:
			brushSize.x++;
			break;
		}
		editorInstance.repaint();
	}

}
