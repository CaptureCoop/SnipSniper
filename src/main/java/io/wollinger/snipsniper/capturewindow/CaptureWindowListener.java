package io.wollinger.snipsniper.capturewindow;

import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class CaptureWindowListener implements KeyListener, MouseListener, MouseMotionListener{
	CaptureWindow wndInstance;
	
	public CaptureWindowListener(CaptureWindow wndInstance) {
		this.wndInstance = wndInstance;
	}

	//Mouse Motion Listener

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		wndInstance.cPoint = mouseEvent.getPoint();
		wndInstance.cPointLive = mouseEvent.getPoint();
	}
	
	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		wndInstance.cPointLive = mouseEvent.getPoint();
	}

	//Mouse Listener

	@Override
	public void mouseClicked(MouseEvent mouseEvent) { }
	
	@Override
	public void mouseEntered(MouseEvent mouseEvent) {
		wndInstance.cPointLive = mouseEvent.getPoint();
	}

	@Override
	public void mouseExited(MouseEvent mouseEvent) { }

	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		if(mouseEvent.getButton() == 1) {
			wndInstance.startPoint = mouseEvent.getPoint();
			wndInstance.startPointTotal = MouseInfo.getPointerInfo().getLocation();
			wndInstance.startedCapture = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		if(mouseEvent.getButton() == 1) {
			wndInstance.cPointAlt = MouseInfo.getPointerInfo().getLocation();
			wndInstance.capture();
		}
	}
	
	//Key Listener
	
	@Override
	public void keyPressed(KeyEvent keyEvent) {
		if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
			wndInstance.sniperInstance.killCaptureWindow();
	}

	@Override
	public void keyReleased(KeyEvent keyEvent) { }

	@Override
	public void keyTyped(KeyEvent keyEvent) { }
}
