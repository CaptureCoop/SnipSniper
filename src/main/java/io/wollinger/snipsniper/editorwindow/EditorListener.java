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
import java.util.logging.Level;

import io.wollinger.snipsniper.editorwindow.stamps.IStamp;
import io.wollinger.snipsniper.utils.*;

import javax.imageio.ImageIO;
import javax.swing.*;

public class EditorListener implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener{
	
	EditorWindow editorInstance;

	ArrayList<BufferedImage> history = new ArrayList<>();

	private boolean openColorChooser = false;

	public EditorListener(EditorWindow editorInstance) {
		this.editorInstance = editorInstance;
		resetHistory();
	}

	public void resetHistory() {
		LogManager.log(editorInstance.getID(), "Reset editor history", Level.INFO);
		history.clear();
		history.add(Utils.copyImage(editorInstance.getImage()));
	}
	
	@Override
	public void mouseClicked(MouseEvent mouseEvent) { }

	@Override
	public void mouseEntered(MouseEvent mouseEvent) { }

	@Override
	public void mouseExited(MouseEvent mouseEvent) { }

	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		if(mouseEvent.getButton() == 3) {
			if(editorInstance.isDirty)
				editorInstance.saveImage();
			editorInstance.kill();
		}
			
		editorInstance.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		editorInstance.input.clearMousePath();
		if(mouseEvent.getButton() == 1) {
			save(editorInstance.getImage().getGraphics(), false);
		}else if(mouseEvent.getButton() == 2) {
			save(editorInstance.getImage().getGraphics(), true);
		}
		editorInstance.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		InputContainer input = editorInstance.input;
		input.addMousePathPoint(mouseEvent.getPoint());
		input.setMousePosition((int) mouseEvent.getPoint().getX(), (int) mouseEvent.getPoint().getY());
		editorInstance.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		editorInstance.input.setMousePosition((int) mouseEvent.getPoint().getX(), (int) mouseEvent.getPoint().getY());
		editorInstance.repaint();
	}

	public void save(Graphics g, boolean isCensor) {
		editorInstance.isDirty = true;
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHints(editorInstance.getQualityHints());
		editorInstance.getSelectedStamp().render(g2, editorInstance.input, true, isCensor, history.size());
		editorInstance.repaint();
		g2.dispose();
		g.dispose();
		history.add(Utils.copyImage(editorInstance.getImage()));
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
		InputContainer input = editorInstance.input;

		if(input.isKeyPressed(KeyEvent.VK_ALT)) {
			IStamp stamp = editorInstance.getSelectedStamp();
			Color oldColor = stamp.getColor().getColor();
			final int alpha = stamp.getColor().getColor().getAlpha();
			float[] hsv = new float[3];
			Color.RGBtoHSB(oldColor.getRed(),oldColor.getGreen(),oldColor.getBlue(),hsv);

			float speed = editorInstance.getConfig().getFloat("hsvColorSwitchSpeed");
			if(mouseWheelEvent.getWheelRotation() == 1)
				hsv[0] += speed;
			else if(mouseWheelEvent.getWheelRotation() == -1)
				hsv[0] -= speed;

			Color newColor = Color.getHSBColor(hsv[0], hsv[1], hsv[2]);
			stamp.setColor(new PBRColor(newColor, alpha));
			editorInstance.repaint();
			return;
		}

		editorInstance.getSelectedStamp().update(input, mouseWheelEvent.getWheelRotation(), null);
		editorInstance.repaint();
	}

	@Override
	public void keyPressed(KeyEvent keyEvent) {
		keyEvent.consume();
		int keyCode = keyEvent.getKeyCode();
		editorInstance.input.setKey(keyCode, true);
		IStamp stamp = editorInstance.getSelectedStamp();

		if(editorInstance.input.areKeysPressed(KeyEvent.VK_ALT, KeyEvent.VK_C))
			openColorChooser = true;

		if(keyCode == KeyEvent.VK_ESCAPE)
			editorInstance.kill();

		if(keyCode == KeyEvent.VK_1)
			editorInstance.setSelectedStamp(0);
		if(keyCode == KeyEvent.VK_2)
			editorInstance.setSelectedStamp(1);
		if(keyCode == KeyEvent.VK_3)
			editorInstance.setSelectedStamp(2);
		if(keyCode == KeyEvent.VK_4)
			editorInstance.setSelectedStamp(3);
		if(keyCode == KeyEvent.VK_5)
			editorInstance.setSelectedStamp(4);

		if(keyCode == KeyEvent.VK_ENTER) {
			JFileChooser chooser = new JFileChooser();
			File file = new File(Utils.constructFilename(EditorWindow.FILENAME_MODIFIER));
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

		if(editorInstance.input.areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_S)) {
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
				editorInstance.setImage(Utils.copyImage(history.get(size)), false);
				for(IStamp cStamp : editorInstance.getStamps())
					cStamp.editorUndo(history.size());
			}
		}

		stamp.update(editorInstance.input, 0, keyEvent);
		editorInstance.repaint();
	}

	@Override
	public void keyReleased(KeyEvent keyEvent) {
		editorInstance.input.setKey(keyEvent.getKeyCode(), false);
		if(openColorChooser) {
			//This fixes an issue with the ALT key getting "stuck" since the key up event is not beeing received if the color window is in the front.
			openColorChooser = false;
			new ColorChooser(editorInstance.getConfig(), "Marker Color", editorInstance.getSelectedStamp().getColor(), editorInstance.getSelectedStamp().getID() + "DefaultColor");
		}
	}

	@Override
	public void keyTyped(KeyEvent keyEvent) { }

}
