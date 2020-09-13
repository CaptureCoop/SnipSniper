package snipsniper.capturewindow;

import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class CaptureWindowListener implements KeyListener, MouseListener, MouseMotionListener{
	CaptureWindow wndInstance;
	
	public CaptureWindowListener(CaptureWindow _wndInstance) {
		wndInstance = _wndInstance;
	}
	
	//Mouse Motion Listener
	
	@Override
	public void mouseDragged(MouseEvent e) {
		wndInstance.cPoint = e.getPoint();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) { }
	
	//Mouse Listener
	
	@Override
	public void mouseClicked(MouseEvent e) { }
	
	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == 1) {
			wndInstance.startPoint = e.getPoint();
			wndInstance.startPointTotal = MouseInfo.getPointerInfo().getLocation();
			wndInstance.startedCapture = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {	
		if(e.getButton() == 1)
			wndInstance.capture();
	}
	
	//Key Listener
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
			wndInstance.sniperInstance.killCaptureWindow();
	}

	@Override
	public void keyReleased(KeyEvent e) { }

	@Override
	public void keyTyped(KeyEvent e) { }
}
