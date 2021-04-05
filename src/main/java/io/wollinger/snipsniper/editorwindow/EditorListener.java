package io.wollinger.snipsniper.editorwindow;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import io.wollinger.snipsniper.utils.ColorChooser;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;
import io.wollinger.snipsniper.utils.Utils;

public class EditorListener implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener{
	
	EditorWindow editorInstance;

	ArrayList<BufferedImage> history = new ArrayList<>();

	public EditorListener(EditorWindow _editWnd) {
		editorInstance = _editWnd;
		history.add(Utils.copyImage(editorInstance.getImage()));
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) { }

	@Override
	public void mouseEntered(MouseEvent arg0) { }

	@Override
	public void mouseExited(MouseEvent arg0) { }

	@Override
	public void mousePressed(MouseEvent arg0) {
		if(arg0.getButton() == 3) {
			editorInstance.saveImage();
			editorInstance.kill();
		}
			
		editorInstance.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if(arg0.getButton() == 1) {
			save(editorInstance.getColor().c, editorInstance.getImage().getGraphics(), false);
		}else if(arg0.getButton() == 2) {
			save(editorInstance.getCensorColor(), editorInstance.getImage().getGraphics(), true);
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
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHints(editorInstance.getQualityHints());
		g2.setColor(color);
		editorInstance.stamps[editorInstance.selectedStamp].render(g2, editorInstance.input, true, isCensor);
		editorInstance.repaint();
		g2.dispose();
		g.dispose();
		history.add(Utils.copyImage(editorInstance.getImage()));
	}

	float currentHSV;
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		InputContainer input = editorInstance.input;

		if(input.isKeyPressed(KeyEvent.VK_ALT)) {
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
		arg0.consume();
		editorInstance.input.setKey(arg0.getKeyCode(), true);

		if(arg0.getKeyCode() == KeyEvent.VK_C)
			new ColorChooser("Marker Color", editorInstance.getColor());

		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
			editorInstance.kill();

		if(arg0.getKeyCode() == KeyEvent.VK_1)
			editorInstance.selectedStamp = 0;
		if(arg0.getKeyCode() == KeyEvent.VK_2)
			editorInstance.selectedStamp = 1;
		if(arg0.getKeyCode() == KeyEvent.VK_3)
			editorInstance.selectedStamp = 2;

		if(arg0.getKeyCode() == KeyEvent.VK_S) {
			editorInstance.saveImage();
			editorInstance.kill();
		}

		if(editorInstance.input.areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_Z)) {
			int size = history.size();
			if(size > 1) {
				size--;
				history.remove(size);
				size--;
				editorInstance.setImage(Utils.copyImage(history.get(size)));
			}
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
