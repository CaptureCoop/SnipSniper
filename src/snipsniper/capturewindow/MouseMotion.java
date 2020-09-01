package snipsniper.capturewindow;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class MouseMotion implements MouseMotionListener{
	CaptureWindow wndInstance;
	public MouseMotion(CaptureWindow _wndInstance) {
		wndInstance = _wndInstance;
	}
	@Override
	public void mouseDragged(MouseEvent arg0) {
		wndInstance.cPoint = arg0.getPoint();
		wndInstance.repaint();
	}
	@Override
	public void mouseMoved(MouseEvent arg0) {
		wndInstance.repaint();
	}
}
