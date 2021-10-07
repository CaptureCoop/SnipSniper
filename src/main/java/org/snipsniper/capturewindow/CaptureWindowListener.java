package org.snipsniper.capturewindow;

import org.snipsniper.config.ConfigHelper;

import javax.swing.*;
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

	private boolean startedCapture = false;
	private boolean stoppedCapture = false;

	private boolean hoverLeft = false;
	private boolean hoverRight = false;
	private boolean hoverTop = false;
	private boolean hoverBottom = false;
	private boolean hoverCenter = false;

	public CaptureWindowListener(CaptureWindow wndInstance) {
		this.wndInstance = wndInstance;
	}

	//Mouse Motion Listener

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		if(!stoppedCapture) {
			cPoint = mouseEvent.getPoint();
		} else if(wndInstance.isAfterDragEnabled()) {
			checkMovement(mouseEvent);
		}
		cPointLive = mouseEvent.getPoint();
	}
	
	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		cPointLive = mouseEvent.getPoint();
		checkMouse();
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
		if(mouseEvent.getButton() == 1 && !isOnSelection()) {
			startPoint = mouseEvent.getPoint();
			cPoint = mouseEvent.getPoint();
			stoppedCapture = false;
			startPointTotal = MouseInfo.getPointerInfo().getLocation();
			if(isPressed(wndInstance.getAfterDragHotkey()) && wndInstance.getAfterDragMode().equalsIgnoreCase("hold"))
				wndInstance.isAfterDragHotkeyPressed = true;
			startedCapture = true;
		} else if (mouseEvent.getButton() == 3) {
			wndInstance.getSniperInstance().killCaptureWindow();
		}
		if(isOnSelection() && stoppedCapture && wndInstance.isAfterDragEnabled())
			checkMouse();
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		if(mouseEvent.getButton() == 1) {
			if(stoppedCapture && wndInstance.isAfterDragEnabled()) {
				Rectangle rect = wndInstance.calcRectangle();
				startPoint.x = rect.x;
				startPoint.y = rect.y;
				cPoint.x = rect.width + rect.x;
				cPoint.y = rect.height + rect.y;
			}

			cPointTotal = new Point(cPoint);
			SwingUtilities.convertPointToScreen(cPointTotal, wndInstance);
			startPointTotal = new Point(startPoint);
			SwingUtilities.convertPointToScreen(startPointTotal, wndInstance);
			stoppedCapture = true;

			if(startPoint.x < 0)
				startPoint.x = 0;
			if(startPoint.y < 0)
				startPoint.y = 0;
			if(cPoint.x > wndInstance.getBounds().width)
				cPoint.x = wndInstance.getBounds().width;
			if(cPoint.y > wndInstance.getBounds().height)
				cPoint.y = wndInstance.getBounds().height;

			if(!wndInstance.isAfterDragEnabled())
				wndInstance.capture(false, false, false, false);
		}
	}
	
	//Key Listener
	
	@Override
	public void keyPressed(KeyEvent keyEvent) {
		keys[keyEvent.getKeyCode()] = true;
		if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
			wndInstance.getSniperInstance().killCaptureWindow();

		if(isPressed(KeyEvent.VK_ENTER) || isPressed(KeyEvent.VK_SPACE))
			wndInstance.capture(false, false, false, false);

		if(isPressed(KeyEvent.VK_CONTROL) && isPressed(KeyEvent.VK_S))
			wndInstance.capture(true, false, false, true);

		if(isPressed(KeyEvent.VK_CONTROL) && isPressed(KeyEvent.VK_C))
			wndInstance.capture(false, true, false, true);

		if(isPressed(KeyEvent.VK_CONTROL) && isPressed(KeyEvent.VK_E))
			wndInstance.capture(false, false, true, true);
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

	public boolean isOnSelection() {
		if(cPoint == null)
			return false;
		Rectangle check = wndInstance.calcRectangle();
		int deadzone = wndInstance.getConfig().getInt(ConfigHelper.PROFILE.afterDragDeadzone);
		check.x -= deadzone;
		check.y -= deadzone;
		check.width += deadzone * 2;
		check.height += deadzone * 2;
		return check.contains(getCurrentPoint(PointType.LIVE));
	}

	public void checkMouse() {
		Rectangle rect = wndInstance.calcRectangle();
		Point livePoint = getCurrentPoint(PointType.LIVE);

		int deadzone = wndInstance.getConfig().getInt(ConfigHelper.PROFILE.afterDragDeadzone);

		hoverTop = false; hoverBottom = false; hoverLeft = false; hoverRight = false;

		if(startedCapture && isOnSelection()) {
			int pointYTop = rect.y - livePoint.y;
			if (pointYTop > -deadzone && pointYTop < deadzone)
				hoverTop = true;

			int pointYBottom = pointYTop + rect.height;
			if(pointYBottom > -deadzone && pointYBottom < deadzone)
				hoverBottom = true;

			int pointXLeft = rect.x - livePoint.x;
			if(pointXLeft > -deadzone && pointXLeft < deadzone)
				hoverLeft = true;

			int pointXRight = pointXLeft + rect.width;
			if(pointXRight > -deadzone && pointXRight < deadzone)
				hoverRight = true;

			Cursor toSet = null;
			if(hoverLeft || hoverRight)
				toSet = new Cursor(Cursor.W_RESIZE_CURSOR);

			if(hoverTop || hoverBottom)
				toSet = new Cursor(Cursor.N_RESIZE_CURSOR);

			if(hoverLeft && hoverTop)
				toSet = new Cursor(Cursor.NW_RESIZE_CURSOR);

			if(hoverRight && hoverTop)
				toSet = new Cursor(Cursor.NE_RESIZE_CURSOR);

			if(hoverBottom && hoverLeft)
				toSet = new Cursor(Cursor.SW_RESIZE_CURSOR);

			if(hoverBottom && hoverRight)
				toSet = new Cursor(Cursor.SE_RESIZE_CURSOR);

			if(!hoverTop && !hoverBottom && !hoverLeft && !hoverRight) {
				hoverCenter = true;
				wndInstance.getRootPane().setCursor(new Cursor(Cursor.MOVE_CURSOR));
			} else {
				hoverCenter = false;
				wndInstance.getRootPane().setCursor(toSet);
			}
		} else {
			wndInstance.getRootPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void checkMovement(MouseEvent mouseEvent) {
		if(hoverTop)
			startPoint.y = cPointLive.y;

		if(hoverBottom)
			cPoint.y = cPointLive.y;

		if(hoverLeft)
			startPoint.x = cPointLive.x;

		if(hoverRight)
			cPoint.x = cPointLive.x;

		if(hoverCenter) {
			Point livePoint = mouseEvent.getPoint();
			int moveX = cPointLive.x - livePoint.x;
			int moveY = cPointLive.y - livePoint.y;
			startPoint.x -= moveX;
			startPoint.y -= moveY;
			cPoint.x -= moveX;
			cPoint.y -= moveY;
		}
	}

	public boolean startedCapture() {
		return startedCapture;
	}
}
