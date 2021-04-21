package io.wollinger.snipsniper.configwindow;

import javax.swing.JButton;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

public class HotKeyButton extends JButton implements NativeKeyListener, NativeMouseListener {
	private static final long serialVersionUID = 8834166293141062833L;
	
	private boolean listening = false;
	public int hotkey;
	private final HotKeyButton instance;
	public boolean isKeyboard = true;

	public HotKeyButton(String key) {
		if(key.startsWith("NONE")) {
			this.setText("NONE");
		} else if(key.startsWith("KB")) {
			hotkey = Integer.parseInt(key.replace("KB", ""));
			this.setText(NativeKeyEvent.getKeyText(hotkey));
		} else if (key.startsWith("M")) {
			hotkey = Integer.parseInt(key.replace("M", ""));
			isKeyboard = false;
			this.setText("Mouse " + hotkey);
		}

		instance = this;
		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
		this.addActionListener(listener -> {
			listening = true;
			instance.setText("...listening");
		});
	}
	
	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
		if(listening) {
			hotkey = nativeKeyEvent.getKeyCode();
			listening = false;
			instance.setText(NativeKeyEvent.getKeyText(hotkey));
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) { }

	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) { }

	@Override
	public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
		if(listening) {
			hotkey = nativeMouseEvent.getButton();

			if(hotkey == 1 || hotkey == 2) {
				hotkey = -1;
				return;
			}
			isKeyboard = false;
			listening = false;
			instance.setText("Mouse " + hotkey);
		}
	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) { }

	@Override
	public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) { }
}
