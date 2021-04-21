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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.wollinger.snipsniper.editorwindow.stamps.IStamp;
import io.wollinger.snipsniper.utils.*;

import javax.imageio.ImageIO;
import javax.swing.*;

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
			if(editorInstance.isDirty)
				editorInstance.saveImage();
			editorInstance.kill();
		}
			
		editorInstance.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		editorInstance.input.clearMousePath();
		if(arg0.getButton() == 1) {
			save(editorInstance.getImage().getGraphics(), false);
		}else if(arg0.getButton() == 2) {
			save(editorInstance.getImage().getGraphics(), true);
		}
		editorInstance.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		InputContainer input = editorInstance.input;
		input.addMousePathPoint(arg0.getPoint());
		input.setMousePosition((int)arg0.getPoint().getX(), (int)arg0.getPoint().getY());
		editorInstance.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		editorInstance.input.setMousePosition((int)arg0.getPoint().getX(), (int)arg0.getPoint().getY());
		editorInstance.repaint();
	}

	public void save(Graphics g, boolean isCensor) {
		editorInstance.isDirty = true;
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHints(editorInstance.getQualityHints());
		editorInstance.stamps[editorInstance.selectedStamp].render(g2, editorInstance.input, true, isCensor, history.size());
		editorInstance.repaint();
		g2.dispose();
		g.dispose();
		history.add(Utils.copyImage(editorInstance.getImage()));
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		InputContainer input = editorInstance.input;

		if(input.isKeyPressed(KeyEvent.VK_ALT)) {
			IStamp stamp = editorInstance.stamps[editorInstance.selectedStamp];
			Color oldColor = stamp.getColor().c;
			final int alpha = stamp.getColor().c.getAlpha();
			float[] hsv = new float[3];
			Color.RGBtoHSB(oldColor.getRed(),oldColor.getGreen(),oldColor.getBlue(),hsv);

			float speed = editorInstance.getSniperInstance().cfg.getFloat("hsvColorSwitchSpeed");
			if(arg0.getWheelRotation() == 1)
				hsv[0] += speed;
			else if(arg0.getWheelRotation() == -1)
				hsv[0] -= speed;

			Color newColor = Color.getHSBColor(hsv[0], hsv[1], hsv[2]);
			stamp.setColor(new PBRColor(newColor, alpha));
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

		if(arg0.getKeyCode() == KeyEvent.VK_C) {
			IStamp stamp = editorInstance.stamps[editorInstance.selectedStamp];
			new ColorChooser(editorInstance.getSniperInstance(), "Marker Color", editorInstance.stamps[editorInstance.selectedStamp].getColor(), stamp.getID() + "DefaultColor");
		}

		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
			editorInstance.kill();

		if(arg0.getKeyCode() == KeyEvent.VK_1)
			editorInstance.selectedStamp = 0;
		if(arg0.getKeyCode() == KeyEvent.VK_2)
			editorInstance.selectedStamp = 1;
		if(arg0.getKeyCode() == KeyEvent.VK_3)
			editorInstance.selectedStamp = 2;
		if(arg0.getKeyCode() == KeyEvent.VK_4)
			editorInstance.selectedStamp = 3;

		if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			JFileChooser chooser = new JFileChooser();
			File file = new File(editorInstance.getSniperInstance().constructFilename(editorInstance.FILENAME_MODIFIER));
			chooser.setSelectedFile(file);
			int result = chooser.showSaveDialog(chooser);
			if(result == JFileChooser.APPROVE_OPTION){
				try {
					if(chooser.getSelectedFile().createNewFile()) {
						ImageIO.write(editorInstance.getImage(), "png", chooser.getSelectedFile());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if(arg0.getKeyCode() == KeyEvent.VK_S) {
			if(editorInstance.isDirty)
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
				for(IStamp stamp : editorInstance.stamps)
					stamp.editorUndo(history.size());
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
