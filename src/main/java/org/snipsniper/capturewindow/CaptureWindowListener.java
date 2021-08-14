package org.snipsniper.capturewindow;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class CaptureWindowListener implements KeyListener, MouseListener, MouseMotionListener{
	private final CaptureWindow wndInstance;
	private final boolean[] keys = new boolean[4096];

	private Point startPoint; //Mouse position given by event *1
	private Point startPointTotal; //Mouse position given by MouseInfo.getPointerInfo (Different then the above in some scenarios) *2
	private Point cPoint; //See *1
	private Point cPointTotal; //See *2
	private Point cPointLive; //Live position, cPoint and cPointTotal are only set once dragging mouse. cPointLive is always set

	public CaptureWindowListener(CaptureWindow wndInstance) {
		this.wndInstance = wndInstance;
	}

	//Mouse Motion Listener

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		cPoint = mouseEvent.getPoint();
		cPointLive = mouseEvent.getPoint();
	}
	
	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		cPointLive = mouseEvent.getPoint();
	}

	//Mouse Listener

	@Override
	public void mouseClicked(MouseEvent mouseEvent) { }
	
	@Override
	public void mouseEntered(MouseEvent mouseEvent) {
		cPointLive = mouseEvent.getPoint();
	}

	@Override
	public void mouseExited(MouseEvent mouseEvent) { }

	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		if(mouseEvent.getButton() == 1) {
			startPoint = mouseEvent.getPoint();
			startPointTotal = MouseInfo.getPointerInfo().getLocation();
			wndInstance.startedCapture = true;
		} else if (mouseEvent.getButton() == 3) {
			wndInstance.getSniperInstance().killCaptureWindow();
		}
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		if(mouseEvent.getButton() == 1) {
			cPointTotal = MouseInfo.getPointerInfo().getLocation();
			wndInstance.capture();
		}
	}
	
	//Key Listener
	
	@Override
	public void keyPressed(KeyEvent keyEvent) {
		keys[keyEvent.getKeyCode()] = true;
		if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
			wndInstance.getSniperInstance().killCaptureWindow();
	}

	@Override
	public void keyReleased(KeyEvent keyEvent) {
		keys[keyEvent.getKeyCode()] = false;
	}

	@Override
	public void keyTyped(KeyEvent keyEvent) { }

	public boolean isPressed(int keyCode) {
		return keys[keyCode];
	}

	public boolean isPressedOnce(int keyCode) {
		boolean returnValue = keys[keyCode];
		keys[keyCode] = false;
		return returnValue;
	}

	public Point getStartPoint(PointType type) {
		switch(type) {
			case NORMAL: return startPoint;
			case TOTAL: return startPointTotal;
		}
		return null;
	}

	public Point getCurrentPoint(PointType type) {
		switch(type) {
			case NORMAL: return cPoint;
			case TOTAL: return cPointTotal;
			case LIVE: return cPointLive;
		}
		return null;
	}
}
