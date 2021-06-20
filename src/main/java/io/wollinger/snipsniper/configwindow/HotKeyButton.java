package io.wollinger.snipsniper.configwindow;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.webkit.event.WCChangeListener;
import io.wollinger.snipsniper.utils.LangManager;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import java.util.ArrayList;

public class HotKeyButton extends JButton implements NativeKeyListener, NativeMouseListener {
	private boolean listening = false;
	public int hotkey;
	private final HotKeyButton instance;
	public boolean isKeyboard = true;

	private String oldLabel;

	private ArrayList<ChangeListener> listeners = new ArrayList<>();

	public HotKeyButton(String key) {
		if(key.startsWith("NONE")) {
			this.setText(LangManager.getItem("config_label_none"));
			hotkey = -1;
		} else if(key.startsWith("KB")) {
			hotkey = Integer.parseInt(key.replace("KB", ""));
			this.setText(NativeKeyEvent.getKeyText(hotkey));
		} else if (key.startsWith("M")) {
			hotkey = Integer.parseInt(key.replace("M", ""));
			isKeyboard = false;
			this.setText(LangManager.getItem("config_label_mouse") + " " + hotkey);
		}

		instance = this;
		GlobalScreen.addNativeKeyListener(this);
		GlobalScreen.addNativeMouseListener(this);
		this.addActionListener(listener -> {
			listening = true;
			instance.setText(LangManager.getItem("config_label_hotkey_listening"));
		});

		oldLabel = instance.getText();
	}
	
	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
		if(listening) {
			if(nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
				listening = false;
				instance.setText(oldLabel);
			} else {
				isKeyboard = true;
				hotkey = nativeKeyEvent.getKeyCode();
				listening = false;
				instance.setText(NativeKeyEvent.getKeyText(hotkey));
				oldLabel = instance.getText();
				notifyListeners();
			}
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
			notifyListeners();
			isKeyboard = false;
			listening = false;
			instance.setText(LangManager.getItem("config_label_mouse") + " " + hotkey);
			oldLabel = instance.getText();
		}
	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) { }

	@Override
	public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) { }

	private void notifyListeners() {
		for(ChangeListener listener : listeners)
			listener.stateChanged(new ChangeEvent(this));
	}

	public void addDoneCapturingListener(ChangeListener listener) {
		listeners.add(listener);
	}
}
