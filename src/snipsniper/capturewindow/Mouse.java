package snipsniper.capturewindow;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Mouse implements MouseListener{

	CaptureWindow wndInstance;

	public Mouse(CaptureWindow _wndInstance) {
		this.wndInstance = _wndInstance;
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) { }
	
	@Override
	public void mouseEntered(MouseEvent arg0) {	}

	@Override
	public void mouseExited(MouseEvent arg0) { }

	@Override
	public void mousePressed(MouseEvent arg0) {
		if(arg0.getButton() == 1) {
			wndInstance.startPoint = arg0.getPoint();
			wndInstance.startPointTotal = MouseInfo.getPointerInfo().getLocation();
			wndInstance.startedCapture = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {	
		if(arg0.getButton() == 1)
			wndInstance.capture();
	}

}
