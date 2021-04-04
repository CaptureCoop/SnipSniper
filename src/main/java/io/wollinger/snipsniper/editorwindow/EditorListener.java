package io.wollinger.snipsniper.editorwindow;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import io.wollinger.snipsniper.editorwindow.stamps.IStamp;
import io.wollinger.snipsniper.utils.ColorChooser;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;
import io.wollinger.snipsniper.utils.Vector2Int;

public class EditorListener implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener{
	
	EditorWindow editorInstance;
	
	Vector2Int startPoint = null;
	
	Vector2Int lastPoint = null;

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
		if(arg0.getButton() == 1 || arg0.getButton() == 2) {
			IStamp stamp = editorInstance.stamps[editorInstance.selectedStamp];
			Vector2Int brushSize = new Vector2Int(stamp.getWidth(), stamp.getHeight());
			startPoint = new Vector2Int(arg0.getPoint().getX() - (float) brushSize.x / 2, arg0.getPoint().getY() - (float) brushSize.y / 2);
			lastPoint = new Vector2Int(arg0.getPoint().getX() + (float) brushSize.x / 2, arg0.getPoint().getY() + (float) brushSize.y / 2);

			if(arg0.getButton() == 1)
				save(editorInstance.getColor().c, editorInstance.getOverdraw().getGraphics(), false);
			else if(arg0.getButton() == 2) {
				save(editorInstance.getCensorColor(), editorInstance.getImage().getGraphics(), true);
				save(editorInstance.getCensorColor(), editorInstance.getOverdraw().getGraphics(), true);
			}

			startPoint = null;
			lastPoint = null;
		}
		

		editorInstance.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		editorInstance.input.setMousePosition((int)arg0.getPoint().getX(), (int)arg0.getPoint().getY());
		editorInstance.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		editorInstance.input.setMousePosition((int)arg0.getPoint().getX(), (int)arg0.getPoint().getY());
		editorInstance.repaint();
	}

	public void save(Color color, Graphics g, boolean isCensor) {
		g.setColor(color);
		editorInstance.stamps[editorInstance.selectedStamp].render(g, editorInstance.input, true, isCensor);
		editorInstance.repaint();
		g.dispose();
	}

	float currentHSV;
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		InputContainer input = editorInstance.input;

		if(input.isKeyPressed(KeyEvent.VK_V)) {
			final Color hsvColor = Color.getHSBColor(currentHSV, 1, 1);

			editorInstance.setColor(new PBRColor(hsvColor.getRed(), hsvColor.getGreen(), hsvColor.getBlue(), editorInstance.getColor().c.getAlpha()));
			if(arg0.getWheelRotation() == 1)
				currentHSV += 0.01F;
			else if(arg0.getWheelRotation() == -1)
				currentHSV -= 0.01F;

			editorInstance.repaint();
			return;
		}

		editorInstance.stamps[editorInstance.selectedStamp].updateSize(input, arg0.getWheelRotation());

		editorInstance.repaint();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		editorInstance.input.setKey(arg0.getKeyCode(), true);

		if(arg0.getKeyCode() == KeyEvent.VK_C)
			new ColorChooser("Marker Color", editorInstance.getColor());

		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
			editorInstance.kill();

		if(arg0.getKeyCode() == KeyEvent.VK_1)
			editorInstance.selectedStamp = 0;
		if(arg0.getKeyCode() == KeyEvent.VK_2)
			editorInstance.selectedStamp = 1;

		if(arg0.getKeyCode() == KeyEvent.VK_S) {
			editorInstance.saveImage();
			editorInstance.kill();
		}

		editorInstance.stamps[editorInstance.selectedStamp].updateSize(editorInstance.input, 0);
		editorInstance.repaint();
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		editorInstance.input.setKey(arg0.getKeyCode(), false);
	}

	@Override
	public void keyTyped(KeyEvent arg0) { }

}
