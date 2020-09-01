package snipsniper.capturewindow;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener{

	CaptureWindow wnd;
	
	public Keyboard(CaptureWindow _wnd) {
		this.wnd = _wnd;
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
			wnd.sniperInstance.killCaptureWindow();
	}

	@Override
	public void keyReleased(KeyEvent arg0) { }

	@Override
	public void keyTyped(KeyEvent arg0) { }

}
