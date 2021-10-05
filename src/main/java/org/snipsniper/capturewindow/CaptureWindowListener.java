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

	private boolean startedCapture = false;
	private boolean stoppedCapture = false;

	private boolean hoverLeft = false;
	private boolean hoverRight = false;
	private boolean hoverTop = false;
	private boolean hoverBottom = false;

	enum DIRECTION {TOP, BOTTOM, LEFT, RIGHT}

	public CaptureWindowListener(CaptureWindow wndInstance) {
		this.wndInstance = wndInstance;
	}

	//Mouse Motion Listener

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		if(!stoppedCapture) {
			cPoint = mouseEvent.getPoint();
		} else {
			if(hoverTop) {
				startPoint.y -= startPoint.y - cPointLive.y;
			}
			if(hoverBottom) {
				cPoint.y -= cPoint.y - cPointLive.y;
			}
			if(hoverLeft) {
				startPoint.x -= startPoint.x - cPointLive.x;
			}
			if(hoverRight) {
				cPoint.x -= cPoint.x - cPointLive.x;
			}
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
		if(mouseEvent.getButton() == 1 && !stoppedCapture) {
			startPoint = mouseEvent.getPoint();
			startPointTotal = MouseInfo.getPointerInfo().getLocation();
			startedCapture = true;
		} else if (mouseEvent.getButton() == 3) {
			wndInstance.getSniperInstance().killCaptureWindow();
		}
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		if(mouseEvent.getButton() == 1) {
			if(!stoppedCapture)
				cPointTotal = MouseInfo.getPointerInfo().getLocation();
			stoppedCapture = true;
			//wndInstance.capture();
			//TODO: testing
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

	public void checkMouse() {
		Rectangle rect = wndInstance.calcRectangle();
		Rectangle check = wndInstance.calcRectangle();
		Point livePoint = getCurrentPoint(PointType.LIVE);

		int margin = 10;

		check.x -= margin;
		check.y -= margin;
		check.width += margin * 2;
		check.height += margin * 2;

		hoverTop = false; hoverBottom = false; hoverLeft = false; hoverRight = false;

		if(startedCapture && check.contains(livePoint)) {
			int pointYTop = rect.y - livePoint.y;
			if (pointYTop > -margin && pointYTop < margin)
				hoverTop = true;

			int pointYBottom = pointYTop + rect.height;
			if(pointYBottom > -margin && pointYBottom < margin)
				hoverBottom = true;

			int pointXLeft = rect.x - livePoint.x;
			if(pointXLeft > -margin && pointXLeft < margin)
				hoverLeft = true;

			int pointXRight = pointXLeft + rect.width;
			if(pointXRight > -margin && pointXRight < margin)
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

			if(!hoverTop && !hoverBottom && !hoverLeft && !hoverRight)
				wndInstance.getRootPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			else
				wndInstance.getRootPane().setCursor(toSet);
		} else {
			wndInstance.getRootPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public boolean startedCapture() {
		return startedCapture;
	}
}
