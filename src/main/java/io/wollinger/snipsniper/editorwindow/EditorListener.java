package io.wollinger.snipsniper.editorwindow;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.ColorChooser;
import io.wollinger.snipsniper.utils.PBRColor;
import io.wollinger.snipsniper.utils.Vector2Int;

public class EditorListener implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener{
	
	EditorWindow editorInstance;
	
	Vector2Int startPoint = null;
	
	Vector2Int lastPoint = null;
	
	Vector2Int mousePos = null;
	
	boolean isCTRL = false;
	boolean isShift = false;
	boolean isV = false;
	
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
			String type = editorInstance.modeToString(editorInstance.getMode());

			Vector2Int brushSize = new Vector2Int(editorInstance.sniperInstance.cfg.getInt("editorStamp" + type + "Width"), editorInstance.sniperInstance.cfg.getInt("editorStamp" + type + "Height"));
			startPoint = new Vector2Int(arg0.getPoint().getX() - (float) brushSize.x / 2, arg0.getPoint().getY() - (float) brushSize.y / 2);
			lastPoint = new Vector2Int(arg0.getPoint().getX() + (float) brushSize.x / 2, arg0.getPoint().getY() + (float) brushSize.y / 2);

			if(arg0.getButton() == 1)
				save(editorInstance.currentColor.c, editorInstance.overdraw.getGraphics(), false);
			else if(arg0.getButton() == 2) {
				save(editorInstance.censorColor, editorInstance.img.getGraphics(), true);
				save(editorInstance.censorColor, editorInstance.overdraw.getGraphics(), true);
			}

			startPoint = null;
			lastPoint = null;
		}
		

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

	//TODO: Fix modes & make them modular
	public void save(Color color, Graphics g, boolean fast) {
		Vector2Int pos = new Vector2Int(lastPoint);
		Vector2Int size = new Vector2Int(startPoint.x - lastPoint.x, startPoint.y - lastPoint.y);
		g.setColor(color);

		if(!editorInstance.sniperInstance.cfg.getBool("smartPixel") || fast) {
			if(startPoint.x < lastPoint.x) {
				pos.x = startPoint.x;
				size.x = lastPoint.x - startPoint.x;
			}

			if(startPoint.y < lastPoint.y) {
				pos.y = startPoint.y;
				size.y = lastPoint.y - startPoint.y;
			}

			g.fillRect(pos.x, pos.y, size.x, size.y);
		} else {
			for (int y = 0; y < -size.y; y++) {
				for (int x = 0; x < -size.x; x++) {
					int posX = pos.x - x;
					int posY = pos.y - y;
					if(posX >= 0 && posY >= 0 && posX < editorInstance.overdraw.getWidth() && posY < editorInstance.overdraw.getHeight()) {

						Color c = new Color(editorInstance.img.getRGB(posX, posY));

						int total = c.getRed() + c.getGreen() + c.getBlue();
						int alpha = (int)((205F/765F) * total + 25);
						Color oC = editorInstance.currentColor.c;
						g.setColor(new Color(oC.getRed(), oC.getGreen(), oC.getBlue(), alpha));
						g.drawLine(posX, posY, posX, posY);
					}
				}
			}
		}

		editorInstance.repaint();
		g.dispose();
	}

	float currentHSV;
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		Config cfg = editorInstance.sniperInstance.cfg;
		if(isV) {
			final Color hsvColor = Color.getHSBColor(currentHSV, 1, 1);

			editorInstance.currentColor = new PBRColor(hsvColor.getRed(), hsvColor.getGreen(), hsvColor.getBlue(), editorInstance.currentColor.c.getAlpha());
			if(arg0.getWheelRotation() == 1)
				currentHSV += 0.01F;
			else if(arg0.getWheelRotation() == -1)
				currentHSV -= 0.01F;

			editorInstance.repaint();
			return;
		}

		if(editorInstance.getMode() == EditorWindow.MODE.CUBE) {
			String dir = "Width";
			if (isShift) dir = "Height";

			String idSize = "editorStampCube" + dir;
			String idSpeed = "editorStampCube" + dir + "Speed";
			String idMinimum = "editorStampCube" + dir + "Minimum";

			int size = cfg.getInt(idSize);

			switch (arg0.getWheelRotation()) {
				case 1:
					size -= cfg.getInt(idSpeed);
					break;
				case -1:
					size += cfg.getInt(idSpeed);
					break;
			}

			if (size <= cfg.getInt(idMinimum)) size = cfg.getInt(idMinimum);
			cfg.set(idSize, size + "");
		} else if (editorInstance.getMode() == EditorWindow.MODE.CIRCLE) {
				final String idSizeWidth = "editorStampCircleWidth";
				final String idSizeHeight = "editorStampCircleHeight";

				String idSpeed = "editorStampCircleSpeed";
				final String idSpeedWidth = "editorStampCircleWidthSpeed";
				final String idSpeedHeight = "editorStampCircleHeightSpeed";

				final String idWidthMin = "editorStampCircleWidthMinimum";
				final String idHeightMin = "editorStampCircleHeightMinimum";

				int sizeWidth = cfg.getInt(idSizeWidth);
				int sizeHeight = cfg.getInt(idSizeHeight);

				boolean doWidth = true;
				boolean doHeight = true;

				if(isCTRL) {
					doWidth = false;
					idSpeed = idSpeedHeight;
				} else if(isShift) {
					doHeight = false;
					idSpeed = idSpeedWidth;
				}

				switch (arg0.getWheelRotation()) {
					case 1:
						if(doWidth) sizeWidth -= cfg.getInt(idSpeed);
						if(doHeight) sizeHeight -= cfg.getInt(idSpeed);
						break;
					case -1:
						if(doWidth) sizeWidth += cfg.getInt(idSpeed);
						if(doHeight) sizeHeight += cfg.getInt(idSpeed);
						break;
				}

				if (sizeWidth <= cfg.getInt(idWidthMin))
					sizeWidth = cfg.getInt(idWidthMin);

				if (sizeHeight <= cfg.getInt(idHeightMin))
					sizeHeight = cfg.getInt(idHeightMin);

				cfg.set(idSizeWidth, sizeWidth + "");
				cfg.set(idSizeHeight, sizeHeight + "");
		}
		
		editorInstance.repaint();
	}

	//TODO: Add all keys into an array to make it easier to check what is pressed
	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_CONTROL)
			isCTRL = true;

		if(arg0.getKeyCode() == KeyEvent.VK_SHIFT)
			isShift = true;

		if(arg0.getKeyCode() == KeyEvent.VK_V)
			isV = true;

		if(arg0.getKeyCode() == KeyEvent.VK_C)
			new ColorChooser("Marker Color", editorInstance.currentColor);

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

		if(arg0.getKeyCode() == KeyEvent.VK_SHIFT)
			isShift = false;

		if(arg0.getKeyCode() == KeyEvent.VK_V)
			isV = false;
	}

	@Override
	public void keyTyped(KeyEvent arg0) { }

}
