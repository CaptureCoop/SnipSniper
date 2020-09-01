package snipsniper.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class HotKeyButton extends JButton implements NativeKeyListener{

	private static final long serialVersionUID = 8834166293141062833L;
	
	private boolean listening = false;
	public int hotkey;
	private HotKeyButton instance;
	
	public HotKeyButton() {
		instance = this;
		GlobalScreen.addNativeKeyListener(this);
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				listening = true;
				instance.setText("...listening");
			}
			
		});
	}
	
	@Override
	public void nativeKeyPressed(NativeKeyEvent arg0) {
		if(listening) {
			hotkey = arg0.getKeyCode();
			listening = false;
			instance.setText(NativeKeyEvent.getKeyText(hotkey));
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) { }

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) { }

}
